import LoginDialog from '@/components/LoginDialog.vue';
import type { UserProfile } from '@/types/deposit';
import { useDrawerService } from '@/utils/drawer.service';
import axios from 'axios';
import { defineStore } from 'pinia';
import { useUserProfileStore } from './users';

const baseUrl = import.meta.env.BASE_URL;

export const useLdapStore = defineStore('LdapStore', () => {
    const userProfileStore = useUserProfileStore();
    const dialog = useDrawerService();

    const requireLogin = async () => {
        await dialog.open(LoginDialog, {
            props: {
                position: 'full', // Standard PrimeVue Drawer prop
                header: 'Login to Continue',
                style: { width: '100vw' }
            }
        });
    };

    const login = async (username: string, password: string) => {
        const user = {} as UserProfile;
        user.username = username;
        user.password = password;

        const response = await axios.post(`${baseUrl}/auth/login`, user);
        if (response.status !== 200) {
            throw new Error(`Login failed: ${response.status} ${response.statusText}`);
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
        // window.location.reload(); // Reload the page to reset the state after logout
        await requireLogin(); // Prompt for login again after logout
    };

    return { requireLogin, login, logout };
});
