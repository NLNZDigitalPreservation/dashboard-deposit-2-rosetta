import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { defineStore } from 'pinia';
import { useDialog } from 'primevue/usedialog';
import { useToast } from 'primevue/usetoast';
import { reactive, ref } from 'vue';

export const globalSettings = reactive({
    id: -1,
    paused: false,
    pausedStartTime: 0,
    pausedEndTime: 0,
    delays: 60,
    delayUnit: 'second'
});

export const depositAccount = reactive({
    id: -1
});

export const useSettingsDepositAccountStore = defineStore('SettingsDepositAccountStore', () => {
    const toast = useToast();
    const dialog = useDialog();
    const rest: UseFetchApis = useFetch();

    const data = ref();
    const selectedRow = ref();

    const fetchData = async () => {
        data.value = await rest.get('/restful/setting/deposit-account/all/get');
        return data.value;
    };

    return { data, selectedRow, fetchData };
});
