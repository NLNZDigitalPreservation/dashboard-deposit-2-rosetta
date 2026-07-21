import router from '@/router';
import { useDrawerService } from '@/utils/drawer.service';
import { useUserProfileStore } from '@/utils/users';
import axios from 'axios';
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useAlertStore } from './alert.store';
import { useSystemInfoStore } from './system.info.store';

const baseUrl = import.meta.env.BASE_URL;

export const useAuthStore = defineStore('AuthStore', () => {
    const userProfile = useUserProfileStore();
    const dialog = useDrawerService();
    const alertStore = useAlertStore();
    const currentUrl = ref('/home.html');

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
        loginUrl.searchParams.set('redirect_uri', currentUrl.value || redirectUri);
        loginUrl.searchParams.set('response_mode', 'query');
        loginUrl.searchParams.set('scope', 'openid profile email offline_access');
        loginUrl.searchParams.set('prompt', 'select_account');
        window.location.assign(loginUrl.toString());
        return;
    };

    const _handleNormalLogout = () => {
        router.push('/login.html');
    };

    return { currentUrl, isAuthenticated, logout };
});
