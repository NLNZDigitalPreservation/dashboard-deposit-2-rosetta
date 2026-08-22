import type { SystemInfo } from '@/types/deposit';
import { PublicClientApplication } from '@azure/msal-browser';
import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { getRootUrl } from './helper';

const baseUrl = import.meta.env.BASE_URL;

export const useSystemInfoStore = defineStore('SystemInfo', () => {
    const _loaded = ref(false);
    const _data = ref({} as SystemInfo);
    const _msalInstance = ref({} as PublicClientApplication);

    const load = async () => {
        const res = await fetch(`${baseUrl}/restful/system-info`);
        if (res.ok) {
            _loaded.value = true;

            _data.value = await res.json();

            const systemInfo = _data.value;
            const authMode = systemInfo.authMode;

            if (authMode === 'entra') {
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
            }
        }
    };

    const data = computed(() => _data.value);
    const loaded = computed(() => _loaded.value);
    const msalInstance = computed(() => _msalInstance.value);

    return { data, load, loaded, msalInstance };
});
