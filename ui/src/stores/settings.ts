import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { defineStore } from 'pinia';
import { useConfirm } from 'primevue/useconfirm';
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
    const confirm = useConfirm();

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

    const deleteConfirm = (rowData: any) => {
        confirm.require({
            message: 'Do you want to delete this record?',
            header: 'Confirmation',
            icon: 'pi pi-exclamation-triangle',
            rejectProps: {
                label: 'Cancel',
                severity: 'secondary',
                outlined: true
            },
            acceptProps: {
                label: 'Delete',
                severity: 'danger'
            },
            accept: () => {
                deleteRow(rowData);
            },
            reject: () => {}
        });
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
        return ret;
    };

    const depositAccountName = async (id: number) => {
        const data: any = queryRow(id);
        if (!data) {
            return '';
        }
        return data.depositUserInstitute + ' | ' + data.depositUserName;
    };

    return { dataList, data, selectedRow, queryAllRows, queryRow, deleteConfirm, deleteRow, saveRow, depositAccountName };
});

export const useSettingsMaterialFlowStore = defineStore('SettingsMaterialFlowStore', () => {
    const toast = useToast();
    const confirm = useConfirm();

    const rest: UseFetchApis = useFetch();

    const dataList = ref();
    const data = ref();
    const selectedRow = ref();

    const id = ref();
    const enabled = ref();
    const depositAccountId = ref();
    const materialFlowId = ref();
    const materialFlowName = ref();
    const producerId = ref();
    const producerName = ref();
    const rootPath = ref();
    const streamLocation = ref();
    const injectionCompleteFileName = ref();
    const maxActiveDays = ref();
    const maxSaveDays = ref();
    const delays = ref();
    const delayUnit = ref();
    const weeklyMaxConcurrency = ref();
    const actualContentDeleteOptions = ref();
    const backupEnabled = ref();
    const actualContentBackupOptions = ref();
    const backupPath = ref();
    const backupSubFolders = ref();
    const auditRst = ref();
    const auditMsg = ref();
    const init = () => {
        id.value = undefined;
        enabled.value = true;
        depositAccountId.value = undefined;
        materialFlowId.value = '';
        materialFlowName.value = '';
        producerId.value = '';
        producerName.value = '';
        rootPath.value = '';
        streamLocation.value = 'content';
        injectionCompleteFileName.value = 'ready-for-ingestion-FOLDER-COMPLETED';
        maxActiveDays.value = 60;
        maxSaveDays.value = 30;
        delays.value = undefined;
        delayUnit.value = undefined;
        weeklyMaxConcurrency.value = [1, 1, 1, 1, 1, 1, 1];
        actualContentDeleteOptions.value = 'notDelete';
        backupEnabled.value = false;
        actualContentBackupOptions.value = 'notBackup';
        backupPath.value = '';
        backupSubFolders.value = '';
        auditRst.value = true;
        auditMsg.value = 'OK';
    };

    const setData = (data: any) => {
        id.value = data.id;
        enabled.value = data.enabled;
        depositAccountId.value = data.depositAccountId;
        materialFlowId.value = data.materialFlowId;
        materialFlowName.value = data.materialFlowName;
        producerId.value = data.producerId;
        producerName.value = data.producerName;
        rootPath.value = data.rootPath;
        streamLocation.value = data.streamLocation;
        injectionCompleteFileName.value = data.injectionCompleteFileName;
        maxActiveDays.value = data.maxActiveDays;
        maxSaveDays.value = data.maxSaveDays;
        delays.value = data.delays;
        delayUnit.value = data.delayUnit;
        weeklyMaxConcurrency.value = data.weeklyMaxConcurrency;
        actualContentDeleteOptions.value = data.actualContentDeleteOptions;
        backupEnabled.value = data.backupEnabled;
        actualContentBackupOptions.value = data.actualContentBackupOptions;
        backupPath.value = data.backupPath;
        backupSubFolders.value = data.backupSubFolders;
        auditRst.value = data.auditRst;
        auditMsg.value = data.auditMsg;
    };

    const queryAllRows = async () => {
        dataList.value = await rest.get('/restful/setting/flow/all/get');
        return dataList.value;
    };

    const queryRow = async (id: number) => {
        data.value = await rest.get('/restful/setting/flow/detail?id=' + id);
        return data.value;
    };

    const deleteConfirm = (rowData: any) => {
        confirm.require({
            message: 'Do you want to delete this record?',
            header: 'Confirmation',
            icon: 'pi pi-exclamation-triangle',
            rejectProps: {
                label: 'Cancel',
                severity: 'secondary',
                outlined: true
            },
            acceptProps: {
                label: 'Delete',
                severity: 'danger'
            },
            accept: () => {
                deleteRow(rowData);
            },
            reject: () => {}
        });
    };
    const deleteRow = async (rowData: any) => {
        if (!rowData) {
            const error = 'The input param can not be null.';
            console.error(error);
            toast.add({ severity: 'error', summary: 'Error: ', detail: error, life: 3000 });
            return;
        }
        const ret = await rest.delete('/restful/setting/flow/delete?id=' + rowData.id, undefined);
        if (ret) {
            toast.add({ severity: 'success', summary: 'Success: ', detail: 'Succeed to delete the deposit account', life: 3000 });
            queryAllRows();
        }
    };

    const saveRow = async () => {
        const rowData = {
            id: id.value,
            enabled: enabled.value,
            depositAccountId: depositAccountId.value,
            materialFlowId: materialFlowId.value,
            materialFlowName: materialFlowName.value,
            producerId: producerId.value,
            producerName: producerName.value,
            rootPath: rootPath.value,
            streamLocation: streamLocation.value,
            injectionCompleteFileName: injectionCompleteFileName.value,
            maxActiveDays: maxActiveDays.value,
            maxSaveDays: maxSaveDays.value,
            delays: delays.value,
            delayUnit: delayUnit.value,
            weeklyMaxConcurrency: weeklyMaxConcurrency.value,
            actualContentDeleteOptions: actualContentDeleteOptions.value,
            backupEnabled: backupEnabled.value,
            actualContentBackupOptions: actualContentBackupOptions.value,
            backupPath: backupPath.value,
            backupSubFolders: backupSubFolders.value,
            auditRst: auditRst.value,
            auditMsg: auditMsg.value
        };

        if (!rowData) {
            const error = 'The input param can not be null.';
            console.error(error);
            toast.add({ severity: 'error', summary: 'Error: ', detail: error, life: 3000 });
            return;
        }
        const ret = await rest.post('/restful/setting/flow/save', rowData);
        if (ret) {
            toast.add({ severity: 'success', summary: 'Success: ', detail: 'Succeed to save the deposit account', life: 3000 });
            queryAllRows();
        }
        return ret;
    };

    return {
        id,
        enabled,
        depositAccountId,
        materialFlowId,
        materialFlowName,
        producerId,
        producerName,
        rootPath,
        streamLocation,
        injectionCompleteFileName,
        maxActiveDays,
        maxSaveDays,
        delays,
        delayUnit,
        weeklyMaxConcurrency,
        actualContentDeleteOptions,
        backupEnabled,
        actualContentBackupOptions,
        backupPath,
        backupSubFolders,
        auditRst,
        auditMsg,
        init,
        setData,
        queryAllRows,
        queryRow,
        deleteConfirm,
        deleteRow,
        saveRow,
        dataList,
        data,
        selectedRow
    };
});
