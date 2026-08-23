import type { UserProfile } from '@/types/deposit';
import { PublicClientApplication } from '@azure/msal-browser';
import axios from 'axios';
import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { getRootUrl } from './helper';
import { useUserProfileStore } from './users';

const baseUrl = import.meta.env.BASE_URL;

export const useMsalStore = defineStore('MsalStore', () => {
    const userProfileStore = useUserProfileStore();

    const _isInitialized = ref(false);
    const _msalInstance = ref({} as PublicClientApplication);

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

        const res = await fetch(`${baseUrl}/restful/system-info`);
        if (!res.ok) {
            throw new Error(`Failed to fetch system info: ${res.status} ${res.statusText}`);
        }

        const systemInfo = await res.json();
        const rootUrl = getRootUrl();
        const msalConfig = {
            auth: {
                clientId: systemInfo.entraClientId,
                authority: `https://login.microsoftonline.com/${systemInfo.entraTenantId}`,
                redirectUri: `${rootUrl}/redirect.html`
            },
            cache: {
                cacheLocation: 'sessionStorage'
            }
        };
        _msalInstance.value = new PublicClientApplication(msalConfig);
        await _msalInstance.value.initialize();
        _isInitialized.value = true;
    };

    const msalInstance = computed(() => _msalInstance.value);

    const _userProfile = async () => {
        await initialize();
        const account = _msalInstance.value.getActiveAccount();
        if (!account) {
            throw new Error('No active account found');
        }

        const tokenResponse = await _msalInstance.value.acquireTokenSilent({
            account: account,
            scopes: ['User.Read']
        });

        const profileResponse = await fetch('https://graph.microsoft.com/v1.0/me', {
            headers: {
                Authorization: `Bearer ${tokenResponse.accessToken}`
            }
        });

        const profile = await profileResponse.json();

        const user = {} as UserProfile;
        user.username = profile.userPrincipalName;
        user.presentationName = profile.displayName;
        user.email = profile.mail || profile.userPrincipalName;
        user.token = tokenResponse.accessToken;
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
        await initialize();
        try {
            if (redirectMode) {
                await _msalInstance.value.loginRedirect({
                    scopes: ['openid', 'profile', 'email']
                });
            } else {
                await _msalInstance.value.loginPopup({
                    scopes: ['openid', 'profile', 'email']
                });
                await login();
            }
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
            await requireLogin();
            return;
        }
        const response = await axios.post(`${baseUrl}/auth/login`, user);
        if (response.status !== 200) {
            await requireLogin();
            return;
        }

        const userData = response.data as any;
        userProfileStore.update(userData);
    };

    const logout = async () => {
        await axios.delete(`${baseUrl}/auth/logout`, {
            headers: {
                Authorization: userProfileStore.token,
                'Content-Type': 'application/json'
            }
        });
        await _requireLogin(true); // Prompt for login again after logout
    };

    return { initialize, msalInstance, userProfile, requireLogin, login, logout };
});
