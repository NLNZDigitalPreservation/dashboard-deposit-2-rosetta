import type { UserProfile } from '@/types/deposit';
import { getAzureAccountInfo } from '@/utils/msal';
import { useSystemInfoStore } from '@/utils/system.info.store';
import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

const KEY_USER_PROFILE = 'dashboard-user-profile';
export const useUserProfileStore = defineStore('userProfile', () => {
    const userInfo = ref<UserProfile>({} as UserProfile);

    const currUserName = computed(() => userInfo.value.presentationName || userInfo.value.email || userInfo.value.username);

    const load = async () => {
        const systemInfoStore = await useSystemInfoStore();
        const authMode = systemInfoStore.data.value.authMode || 'test';
        if (authMode === 'entra') {
            const tenantId = systemInfoStore.data.value.entraTenantId;
            const clientId = systemInfoStore.data.value.entraClientId;
            const redirectUrl = systemInfoStore.data.value.entraRedirectUrl;

            const azureInfo = await getAzureAccountInfo(tenantId, clientId, redirectUrl);
            userInfo.value.token = '';
            userInfo.value.username = azureInfo?.username || '';
            userInfo.value.presentationName = azureInfo?.presentation_name || '';
            userInfo.value.email = azureInfo?.email || '';
            userInfo.value.role = '';
        } else {
            const cachedContent = localStorage.getItem(KEY_USER_PROFILE);
            if (!cachedContent) {
                return;
            }
            const p = JSON.parse(cachedContent);
            userInfo.value = p;
        }
    };

    const clear = () => {
        userInfo.value = {} as UserProfile;
        localStorage.removeItem(KEY_USER_PROFILE);
    };

    const update = async (sessionInfo: any) => {
        userInfo.value.token = sessionInfo.sessionId;

        const systemInfoStore = await useSystemInfoStore();
        const authMode = systemInfoStore.data.value.authMode || 'test';
        if (authMode !== 'entra') {
            userInfo.value.username = sessionInfo.username;
            userInfo.value.presentationName = sessionInfo.displayName;
        }

        localStorage.setItem(KEY_USER_PROFILE, JSON.stringify(userInfo.value));
    };

    return { userInfo, currUserName, load, clear, update };
});
