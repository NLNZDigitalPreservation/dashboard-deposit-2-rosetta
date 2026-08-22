import router from '@/router';
import { useDrawerService } from '@/utils/drawer.service';
import { useUserProfileStore } from '@/utils/users';
import { PublicClientApplication } from '@azure/msal-browser';
import axios from 'axios';
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useAlertStore } from './alert.store';
import { useSystemInfoStore } from './system.info.store';
const baseUrl = import.meta.env.BASE_URL;

const msalConfig = {
    auth: {
        clientId: 'ac0fafa0-73ae-48c7-bbc9-4ccb7b87086b',
        authority: 'https://login.microsoftonline.com/f659ca5c-fc47-4e96-b24d-14c95df13acb',
        redirectUri: 'https://dps.natlib.govt.nz/depdash/redirect.html'
    },
    cache: {
        cacheLocation: 'sessionStorage'
    }
};
export const useAuthStore = defineStore('AuthStore', () => {
    const systemInfoStore = useSystemInfoStore();
    const userProfile = useUserProfileStore();
    const dialog = useDrawerService();
    const alertStore = useAlertStore();
    const currentUrl = ref('/home.html');

    const login = async () => {
        if (!systemInfoStore.loaded) {
            await systemInfoStore.load();
        }
        const systemInfo = systemInfoStore.data;
        const authMode = systemInfo.authMode;

        if (authMode === 'entra') {
            const msalInstance = systemInfoStore.msalInstance;
            try {
                // Check if user is already authenticated
                let account = msalInstance.getActiveAccount();
                if (!account) {
                    const loginResponse = await msalInstance.loginPopup({
                        scopes: ['openid', 'profile', 'email']
                    });
                    account = loginResponse.account;
                }

                const tokenResponse = await msalInstance.acquireTokenSilent({
                    account: account,
                    scopes: ['User.Read']
                });

                const profileResponse = await fetch('https://graph.microsoft.com/v1.0/me', {
                    headers: {
                        Authorization: `Bearer ${tokenResponse.accessToken}`
                    }
                });

                const profile = await profileResponse.json();

                const user = {
                    name: profile.displayName,
                    email: profile.mail || profile.userPrincipalName,
                    oid: profile.id,
                    tenantId: profile.tenantId
                };

                // await msalInstance.loginRedirect({
                //     scopes: ['openid', 'profile', 'email']
                // });
            } catch (error) {
                console.error('Login failed:', error);
            }

            currentUrl.value = window.location.href;
            const clientId = systemInfo.entraClientId;
            const redirectUri = systemInfo.entraRedirectUrl || currentUrl.value;
            _handleEntraIdLogin(clientId, redirectUri);
        } else {
            currentUrl.value = router.currentRoute.value.fullPath;
            if (currentUrl.value === '/login.html') {
                currentUrl.value = '/home.html';
            }
            _handleNormalLogin();
        }
    };

    const _handleEntraIdLogin = (clientId: string, redirectUri: string) => {
        const loginUrl = new URL('https://login.microsoftonline.com/common/oauth2/v2.0/authorize');
        loginUrl.searchParams.set('client_id', clientId);
        login;
        const msalInstance = new PublicClientApplication(msalConfig);
        try {
            await msalInstance.loginRedirect({
                scopes: ['openid', 'profile', 'email']
            });
        } catch (error) {
            console.error('Login failed:', error);
        }
    };

    const requireLogin = async () => {
        const msalInstance = new PublicClientApplication(msalConfig);
        try {
            await msalInstance.loginRedirect({
                scopes: ['openid', 'profile', 'email']
            });
        } catch (error) {
            console.error('Login failed:', error);
        }
    };

    const isAuthenticated = async () => {
        const userInfo = userProfile.userInfo;
        if (!userInfo?.token) {
            return false;
        }
        const rsp = await axios.post(`${baseUrl}/auth/login.json`, {
            token: userInfo.token,
            username: userInfo.username,
            presentationName: userInfo.presentationName
        });
        return rsp.status === 200;
    };

    const logout = async () => {
        const userInfo = userProfile.userInfo;
        if (userInfo?.token) {
            const rsp = await axios.post(`${baseUrl}/auth/logout.json`, {
                token: userInfo.token
            });
        }
        userProfile.clear();

        const systemInfoStore = useSystemInfoStore();
        const systemInfo = systemInfoStore.data;
        const authMode = systemInfo.authMode;
        if (authMode === 'entra') {
            currentUrl.value = window.location.href;
            const clientId = systemInfo.entraClientId;
            const redirectUri = systemInfo.entraRedirectUrl || currentUrl.value;
            _handleEntraIdLogout(clientId, redirectUri);
        } else {
            currentUrl.value = router.currentRoute.value.fullPath;
            if (currentUrl.value === '/login.html') {
                currentUrl.value = '/home.html';
            }
            _handleNormalLogout();
        }
    };

    const _handleEntraIdLogout = (clientId: string, redirectUri: string) => {
        const loginUrl = new URL('https://login.microsoftonline.com/common/oauth2/v2.0/authorize');
        loginUrl.searchParams.set('client_id', clientId);
        loginUrl.searchParams.set('response_type', 'code');
        loginUrl.searchParams.set('redirect_uri', 'https://azurediagovt.sharepoint.com/_forms/default.aspx');
        loginUrl.searchParams.set('response_mode', 'query');
        loginUrl.searchParams.set('scope', 'openid profile email offline_access');
        loginUrl.searchParams.set('prompt', 'select_account');
        window.location.assign(loginUrl.toString());
        return;
    };

    const _handleNormalLogout = () => {
        router.push('/login.html');
    };

    return { requireLogin, currentUrl, isAuthenticated, logout };
});
