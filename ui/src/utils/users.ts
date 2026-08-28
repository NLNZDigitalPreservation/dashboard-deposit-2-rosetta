import type { UserProfile } from '@/types/deposit';
import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

const KEY_USER_PROFILE = 'dashboard-user-profile';

export const useUserProfileStore = defineStore('userProfile', () => {
    const userInfo = ref<any>({} as any);

    const token = computed(() => userInfo.value.sessionId);
    const presentationName = computed(() => userInfo.value.displayName || userInfo.value.email || userInfo.value.username);
    const currUserName = computed(() => userInfo.value.displayName || userInfo.value.email || userInfo.value.username);

    const load = async () => {
        const cachedContent = localStorage.getItem(KEY_USER_PROFILE);
        if (!cachedContent) {
            return;
        }
        const p = JSON.parse(cachedContent);
        userInfo.value = p;
    };

    const clear = () => {
        userInfo.value = {} as UserProfile;
        localStorage.removeItem(KEY_USER_PROFILE);
    };

    const update = async (userProfile: any) => {
        userProfile.password = '';
        userInfo.value = userProfile;
        localStorage.setItem(KEY_USER_PROFILE, JSON.stringify(userInfo.value));
    };

    return { userInfo, token, presentationName, currUserName, load, clear, update };
});
