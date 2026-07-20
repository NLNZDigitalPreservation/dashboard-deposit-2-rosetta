import type { SystemInfo } from '@/types/deposit';
import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

export const useSystemInfoStore = defineStore('SystemInfo', () => {
    const _data = ref({} as SystemInfo);
    const load = async () => {
        const res = await fetch('/restful/system-info');
        if (res.ok) {
            _data.value = await res.json();
        }
    };

    const data = computed(() => _data.value);

    return { data, load };
});
