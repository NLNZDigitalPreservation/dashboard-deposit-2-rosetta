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

    const dataList = ref();
    const data = ref();
    const selectedRow = ref();

    const queryAllRows = async () => {
        dataList.value = await rest.get('/restful/setting/deposit-account/all/get');
        return dataList.value;
    };

    const queryRow = async (id: number) => {
        data.value = await rest.get('/restful/setting/deposit-account/detail?id=' + id);
        return data.value;
    };

    const deleteRow = async (rowData: any) => {
        if (!rowData) {
            const error = 'The input param can not be null.';
            console.error(error);
            toast.add({ severity: 'error', summary: 'Error: ', detail: error, life: 3000 });
            return;
        }
        const ret = await rest.delete('/restful/setting/deposit-account/delete?id=' + rowData.id, undefined);
        if (ret) {
            toast.add({ severity: 'success', summary: 'Success: ', detail: 'Succeed to delete the deposit account', life: 3000 });
            queryAllRows();
        }
    };

    const saveRow = async (rowData: any) => {
        if (!rowData) {
            const error = 'The input param can not be null.';
            console.error(error);
            toast.add({ severity: 'error', summary: 'Error: ', detail: error, life: 3000 });
            return;
        }
        const ret = await rest.post('/restful/setting/deposit-account/save', rowData);
        if (ret) {
            toast.add({ severity: 'success', summary: 'Success: ', detail: 'Succeed to save the deposit account', life: 3000 });
            queryAllRows();
        }
    };

    return { dataList, data, selectedRow, queryAllRows, queryRow, deleteRow, saveRow };
});
