<template>
    <div v-if="visibleDrawerDepositAccount">
        <Toast />
        <ConfirmDialog></ConfirmDialog>
        <Drawer v-model:visible="visibleDrawerDepositAccount" header="Deposit Accounts" class="!w-full md:!w-80 lg:!w-[50rem]" position="right">
            <DataTable v-model:selection="depositAccountStore.selectedRow" :value="depositAccountStore.dataList" :metaKeySelection="metaKey" dataKey="id" @rowSelect="onRowSelect" tableStyle="width:100%" showGridlines sortField="id" :sortOrder="1">
                <Column field="id" header="ID" sortable></Column>
                <Column field="depositUserInstitute" header="Institute" sortable></Column>
                <Column field="depositUserName" header="User Name" sortable></Column>
                <Column field="id" header="Action" alignFrozen="right" style="width: 8rem">
                    <template #body="{ data }">
                        <Button icon="pi pi-pen-to-square" @click="onEdit(data)" text />
                        <Button icon="pi pi-trash" severity="danger" @click="onDelete(data)" text />
                    </template>
                </Column>
            </DataTable>

            <template #footer>
                <Fluid>
                    <Button label="New" icon="pi pi-plus" @click="onNew()" raised />
                </Fluid>
            </template>
        </Drawer>

        <Dialog v-model:visible="visibleDialogDepositAccount" modal header="Edit Profile" :style="{ width: '55rem' }">
            <template #header>
                <div class="inline-flex items-center justify-center gap-2">
                    <span class="font-bold whitespace-nowrap">Deposit Account: {{ selectedRow.id }}</span>
                </div>
            </template>
            <Fluid>
                <Fieldset legend="Settings">
                    <FlatInputGroup class="mt-2 mb-2">
                        <InputGroupAddon>ID</InputGroupAddon>
                        <InputNumber v-model="selectedRow.id" readonly />
                    </FlatInputGroup>
                    <FlatInputGroup class="mt-2 mb-2">
                        <InputGroupAddon>Institute</InputGroupAddon>
                        <InputText v-model="selectedRow.depositUserInstitute" />
                    </FlatInputGroup>
                    <FlatInputGroup class="mt-2 mb-2">
                        <InputGroupAddon>User Name</InputGroupAddon>
                        <InputText v-model="selectedRow.depositUserName" />
                    </FlatInputGroup>
                    <FlatInputGroup class="mt-2 mb-2">
                        <InputGroupAddon>Password</InputGroupAddon>
                        <Password v-model="selectedRow.depositUserPassword" :feedback="false" />
                    </FlatInputGroup>
                </Fieldset>

                <Fieldset legend="Health Audit">
                    <Message v-if="selectedRow.auditRst" severity="success" :closable="false">{{ selectedRow.auditMsg }}</Message>
                    <Message v-if="!selectedRow.auditRst" severity="warn" :closable="false">{{ selectedRow.auditMsg }}</Message>
                </Fieldset>
            </Fluid>
            <template #footer>
                <Button label="Save" @click="onSave()" autofocus />
                <Button label="Cancel" outlined @click="visibleDialogDepositAccount = false" autofocus />
            </template>
        </Dialog>
    </div>
</template>

<script setup lang="ts">
import { useSettingsDepositAccountStore } from '@/stores/settings';
import { type DepositAccount } from '@/types/deposit';
import { defineExpose, onMounted, ref } from 'vue';

const visibleDialogDepositAccount = ref(false);

const depositAccountStore = useSettingsDepositAccountStore();

const initialData: DepositAccount = {
    id: undefined,
    depositUserInstitute: '',
    depositUserName: '',
    depositUserPassword: '',
    auditRst: true,
    auditMsg: 'OK'
};
const selectedRow = ref(Object.assign({}, initialData));
const metaKey = ref(true);

const onNew = () => {
    selectedRow.value = Object.assign({}, initialData);
    visibleDialogDepositAccount.value = true;
};
const onDelete = (selectedData: any) => {
    depositAccountStore.deleteConfirm(selectedData);
};
const onEdit = (selectedData: any) => {
    selectedRow.value = Object.assign({}, selectedData);
    visibleDialogDepositAccount.value = true;
};
const onSave = async () => {
    const ret = await depositAccountStore.saveRow(selectedRow.value);
    if (ret) {
        visibleDialogDepositAccount.value = false;
    }
};
const onRowSelect = (event: any) => {
    selectedRow.value = event.data;
};

onMounted(() => {
    depositAccountStore.queryAllRows();
});

const visibleDrawerDepositAccount = ref(false);
const toggle = () => {
    visibleDrawerDepositAccount.value = !visibleDrawerDepositAccount.value;
};
defineExpose({ toggle });
</script>
