import type { SystemInfo } from '@/types/deposit';
import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

const baseUrl = import.meta.env.BASE_URL;

export const useSystemInfoStore = defineStore('SystemInfo', () => {
    const _loaded = ref(false);
    const _data = ref({} as SystemInfo);

    const load = async () => {
        if (_loaded.value) {
            return;
        }

        const res = await fetch(`${baseUrl}/restful/system-info`);
        if (res.ok) {
            _loaded.value = true;

            _data.value = await res.json();
        }
    };

    const data = computed(() => _data.value);
    const loaded = computed(() => _loaded.value);

    return { data, load, loaded };
});
