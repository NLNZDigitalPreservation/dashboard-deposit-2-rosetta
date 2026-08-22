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

    const userProfile = async () => {
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

    const requireLogin = async () => {
        await initialize();
        await _msalInstance.value.loginPopup({
            scopes: ['openid', 'profile', 'email']
        });
        await _login();
    };

    const _login = async () => {
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

        const userData = response.data as UserProfile;
        userProfileStore.update(userData);
    };

    const logout = async () => {
        await axios.delete(`${baseUrl}/auth/logout`, {
            headers: {
                Authorization: userProfileStore.token,
                'Content-Type': 'application/json'
            }
        });
        await _msalInstance.value.logoutPopup();
        await _login();
    };

    return { initialize, msalInstance, userProfile, requireLogin, logout };
});
