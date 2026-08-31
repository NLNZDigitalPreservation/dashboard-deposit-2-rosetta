import { useUserProfileStore } from '@/utils/users';
import axios from 'axios';
import { defineStore } from 'pinia';
import { useLdapStore } from './ldap';
import { useMsalStore } from './msal';
import { useSystemInfoStore } from './system.info.store';

const baseUrl = import.meta.env.BASE_URL;

export const useAuthStore = defineStore('AuthStore', () => {
    const systemInfoStore = useSystemInfoStore();
    const msalStore = useMsalStore();
    const ldapStore = useLdapStore();
    const userProfileStore = useUserProfileStore();

    const isLogin = async () => {
        try {
            userProfileStore.load();
            if (!systemInfoStore.loaded) {
                await systemInfoStore.load();
            }

            const systemInfo = systemInfoStore.data;
            const authMode = systemInfo.authMode;

            if (authMode === 'entra') {
                const user = await msalStore.userProfile();
                if (!user) {
                    return false;
                } else {
                    const ret = await msalStore.login();
                    return ret;
                }
            } else {
                const rsp = await axios.get(`${baseUrl}/restful/auth/login`, {
                    headers: {
                        Authorization: userProfileStore.token,
                        'Content-Type': 'application/json'
                    }
                });
                return rsp.status === 200;
            }
        } catch (error) {
            // Do nothing
            // console.error('Error checking login status:', error);
            return false;
        }
    };

    const requireLogin = async (redirectMode = false) => {
        if (!systemInfoStore.loaded) {
            await systemInfoStore.load();
        }

        const systemInfo = systemInfoStore.data;
        const authMode = systemInfo.authMode;

        if (authMode === 'entra') {
            await msalStore.requireLogin(true);
        } else {
            await ldapStore.requireLogin();
        }
    };

    const getAvatar = async () => {
        const systemInfo = systemInfoStore.data;
        const authMode = systemInfo.authMode;

        if (authMode === 'entra') {
            return await msalStore.getAvatar();
        }
        return null;
    };

    const logout = async () => {
        if (!systemInfoStore.loaded) {
            await systemInfoStore.load();
        }

        const systemInfo = systemInfoStore.data;
        const authMode = systemInfo.authMode;

        if (authMode === 'entra') {
            await msalStore.logout();
        } else {
            await ldapStore.logout();
        }
    };

    return { isLogin, requireLogin, getAvatar, logout };
});
