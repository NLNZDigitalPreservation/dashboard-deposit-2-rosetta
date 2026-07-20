import LoginDialog from '@/components/LoginDialog.vue';
import { useDrawerService } from '@/utils/drawer.service';
import { useUserProfileStore } from '@/utils/users';
import axios from 'axios';
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useSystemInfoStore } from './system.info.store';

const baseUrl = import.meta.env.BASE_URL;

export const useAuthStore = defineStore('AuthStore', () => {
    const userProfile = useUserProfileStore();
    const dialog = useDrawerService();
    const isAuthenticating = ref(false);

    const requireLogin = async () => {
        if (isAuthenticating.value) {
            return;
        }

        try {
            isAuthenticating.value = true;
            const result = await dialog.open(LoginDialog, {
                props: {
                    position: 'full', // Standard PrimeVue Drawer prop
                    header: 'Login to Continue',
                    style: { width: '100vw' }
                }
            });
            if (result) {
                console.log('Logged in as:', result.userInfo.role);
            }
        } finally {
            isAuthenticating.value = false;
        }
    };

    const isAuthenticated = async () => {
        const userInfo = userProfile.userInfo;
        if (!userInfo?.token) {
            return false;
        }
        const rsp = await axios.post(`${baseUrl}auth/login.json`, {
            token: userInfo.token,
            username: userInfo.username,
            presentationName: userInfo.presentationName
        });
        return rsp.status === 200;
    };

    const logout = async () => {
        const userInfo = userProfile.userInfo;
        if (!userInfo?.token) {
            return false;
        }
        const rsp = await axios.post(`${baseUrl}auth/logout.json`, {
            token: userInfo.token
        });

        if (userInfo?.token) {
            await fetch(`${baseUrl}/auth/logout.json` + userInfo.token, {
                method: 'DELETE',
                redirect: 'error'
            });
        }
        userProfile.clear();

        const systemInfoStore = useSystemInfoStore();
        const systemInfo = systemInfoStore.data;
        const authMode = systemInfo.authMode;
        if (authMode === 'entra') {
            const clientId = systemInfo.entraClientId;
            const redirectUri = systemInfo.entraRedirectUrl || window.location.origin;
            _handleEntraIdLogout(clientId, redirectUri);
        } else {
            await requireLogin();
        }
    };

    const _handleEntraIdLogout = (clientId: string, redirectUri: string) => {
        const loginUrl = new URL('https://login.microsoftonline.com/common/oauth2/v2.0/authorize');
        loginUrl.searchParams.set('client_id', clientId);
        loginUrl.searchParams.set('response_type', 'code');
        loginUrl.searchParams.set('redirect_uri', redirectUri);
        loginUrl.searchParams.set('response_mode', 'query');
        loginUrl.searchParams.set('scope', 'openid profile email offline_access');
        loginUrl.searchParams.set('prompt', 'select_account');
        window.location.assign(loginUrl.toString());
        return;
    };

    return { requireLogin, isAuthenticating, isAuthenticated, logout };
});
