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
            const rsp = await axios.get(`${baseUrl}/auth/is-login`, {
                headers: {
                    Authorization: userProfileStore.token,
                    'Content-Type': 'application/json'
                }
            });

            if (rsp.status !== 200) {
                return false;
            }

            if (!systemInfoStore.loaded) {
                await systemInfoStore.load();
            }

            const systemInfo = systemInfoStore.data;
            const authMode = systemInfo.authMode;

            if (authMode === 'entra') {
                const user = await msalStore.userProfile();
                if (!user) {
                    return false;
                }
            }
        } catch (error) {
            // Do nothing
            // console.error('Error checking login status:', error);
            return false;
        }

        return true;
    };

    const requireLogin = async (redirectMode = false) => {
        if (!systemInfoStore.loaded) {
            await systemInfoStore.load();
        }

        const systemInfo = systemInfoStore.data;
        const authMode = systemInfo.authMode;

        if (authMode === 'entra') {
            await msalStore.requireLogin(redirectMode);
        } else {
            await ldapStore.requireLogin();
        }
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

    return { isLogin, requireLogin, logout };
});
