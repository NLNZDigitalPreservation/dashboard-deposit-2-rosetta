import type { UserProfile } from '@/types/deposit';
import { PublicClientApplication } from '@azure/msal-browser';
import axios from 'axios';
import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { getRootUrl, sleep } from './helper';
import { useSystemInfoStore } from './system.info.store';
import { useUserProfileStore } from './users';

const baseUrl = import.meta.env.BASE_URL;
const loginOptions = {
    scopes: ['openid', 'profile', 'email', 'User.Read'],
    prompt: 'select_account'
};

export const useMsalStore = defineStore('MsalStore', () => {
    const systemInfoStore = useSystemInfoStore();
    const userProfileStore = useUserProfileStore();

    const _isInitialized = ref(false);
    const _msalInstance = ref({} as any);

    const initialize = async () => {
        if (_isInitialized.value) {
            try {
                const result = await _msalInstance.value.handleRedirectPromise();
                if (result?.account) {
                    _msalInstance.value.setActiveAccount(result.account);
                }
                return;
            } catch (error) {
                console.error('Failed to handle redirect promise:', error);
            }
            return;
        }

        if (!systemInfoStore.loaded) {
            await systemInfoStore.load();
        }
        const systemInfo = systemInfoStore.data;

        const rootUrl = getRootUrl();
        const msalConfig = {
            auth: {
                clientId: systemInfo.entraClientId,
                authority: `https://login.microsoftonline.com/${systemInfo.entraTenantId}`,
                redirectUri: `${rootUrl}/redirect.html`,
                navigateToLoginRequestUrl: true
            },
            cache: {
                cacheLocation: 'localStorage',
                storeAuthStateInCookie: true
            }
        };
        try {
            _msalInstance.value = new PublicClientApplication(msalConfig);
        } catch (error) {
            console.error('Failed to create MSAL instance:', error);
            throw error;
        }
        await _msalInstance.value.initialize();
        _isInitialized.value = true;
    };

    const msalInstance = computed(() => _msalInstance.value);

    const getAvatar = async () => {
        await initialize();

        const account = _msalInstance.value.getActiveAccount();

        if (!account) {
            return null;
        }

        try {
            const tokenResponse = await _msalInstance.value.acquireTokenSilent({
                account,
                scopes: ['User.Read']
            });

            const avatarResponse = await fetch('https://graph.microsoft.com/v1.0/me/photo/$value', {
                headers: {
                    Authorization: `Bearer ${tokenResponse.accessToken}`
                }
            });
            if (!avatarResponse.ok) {
                console.error('Failed to fetch avatar:', avatarResponse.status, avatarResponse.statusText);
                return null;
            }

            const blob = await avatarResponse.blob();
            return URL.createObjectURL(blob);
        } catch (error) {
            console.error('Failed to get avatar:', error);
            return null;
        }
    };

    const handleRedirectPromise = async () => {
        await initialize();
        try {
            await sleep(2000); // Wait for 1 second to ensure MSAL is initialized

            const result = await _msalInstance.value.handleRedirectPromise();
            if (result?.account) {
                _msalInstance.value.setActiveAccount(result.account);
            }
        } catch (error) {
            console.error('Failed to handle redirect promise:', error);
        }
    };

    const _userProfile = async () => {
        await initialize();

        let account = _msalInstance.value.getActiveAccount();
        if (!account) {
            // throw new Error('No active account found');
            const accounts = _msalInstance.value.getAllAccounts();
            if (accounts.length > 0) {
                account = accounts[0];
                _msalInstance.value.setActiveAccount(account);
            }
        }
        if (!account) {
            throw new Error('No active account found');
        }

        const user = {} as UserProfile;

        user.email = account.username;
        user.username = account.username;
        // user.token = account.idToken;
        user.presentationName = account.name;
        // user.role = 'admin';

        return user;
    };

    const userProfile = async () => {
        try {
            return await _userProfile();
        } catch (error) {
            console.error('Failed to get user profile:', error);
            return undefined;
        }
    };

    const _requireLogin = async (redirectMode = false) => {
        try {
            await initialize();
            await _msalInstance.value.loginRedirect(loginOptions);
            console.log('Login successful');
        } catch (error) {
            console.error('Login failed:', error);
            return;
        }
    };

    const requireLogin = async (redirectMode = false) => {
        const user = await userProfile();
        if (user) {
            await login();
        } else {
            await _requireLogin(redirectMode);
        }
    };

    const login = async () => {
        const user = await userProfile();
        if (!user) {
            return;
        }
        const response = await axios.post(`${baseUrl}/restful/auth/login`, user);
        if (response.status !== 200) {
            return;
        }

        const userData = response.data;
        userProfileStore.update(userData);
    };

    const logout = async () => {
        await axios.delete(`${baseUrl}/restful/auth/login`, {
            headers: {
                Authorization: userProfileStore.token,
                'Content-Type': 'application/json'
            }
        });
        _msalInstance.value.setActiveAccount(null);
        await _requireLogin(true); // Prompt the user to log in again after logout
    };

    return { initialize, handleRedirectPromise, msalInstance, getAvatar, userProfile, requireLogin, login, logout };
});
