<template>
    <Toast />
    <ConfirmDialog></ConfirmDialog>
    <Drawer v-model:visible="visible" header="Deposit Accounts" class="!w-full md:!w-80 lg:!w-[50rem]" position="right">
        <DataTable v-model:selection="depositAccount.selectedRow" :value="depositAccount.dataList" :metaKeySelection="metaKey" dataKey="id" @rowSelect="onRowSelect" tableStyle="width:100%" showGridlines sortField="id" :sortOrder="1">
            <Column field="id" header="ID" sortable></Column>
            <Column field="depositUserInstitute" header="Institute" sortable></Column>
            <Column field="depositUserName" header="User Name" sortable></Column>
            <Column field="auditMsg" header="Audit Result" sortable> </Column>
            <Column field="id" header="Action" alignFrozen="right">
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

    <Dialog v-model:visible="visibleDialog" modal header="Edit Profile" :style="{ width: '45rem' }">
        <template #header>
            <div class="inline-flex items-center justify-center gap-2">
                <span class="font-bold whitespace-nowrap">Deposit Account: {{ selectedRow.id }}</span>
            </div>
        </template>
        <Fluid>
            <Fieldset legend="Settings">
                <InputGroup class="mt-2 mb-2">
                    <InputGroupAddon>ID</InputGroupAddon>
                    <InputText v-model="selectedRow.id" disabled="true" />
                </InputGroup>
                <InputGroup class="mt-2 mb-2">
                    <InputGroupAddon>Institute</InputGroupAddon>
                    <InputText v-model="selectedRow.depositUserInstitute" />
                </InputGroup>
                <InputGroup class="mt-2 mb-2">
                    <InputGroupAddon>User Name</InputGroupAddon>
                    <InputText v-model="selectedRow.depositUserName" />
                </InputGroup>
                <InputGroup class="mt-2 mb-2">
                    <InputGroupAddon>Password</InputGroupAddon>
                    <Password v-model="selectedRow.depositUserPassword" :feedback="false" />
                </InputGroup>
            </Fieldset>

            <Fieldset legend="Health Audit">
                <Message v-if="selectedRow.auditRst" severity="success" :closable="false">{{ selectedRow.auditMsg }}</Message>
                <Message v-if="!selectedRow.auditRst" severity="warn" :closable="false">{{ selectedRow.auditMsg }}</Message>
            </Fieldset>
        </Fluid>
        <template #footer>
            <Button label="Save" @click="onSave()" autofocus />
            <Button label="Cancel" outlined @click="visibleDialog = false" autofocus />
        </template>
    </Dialog>
</template>

<script setup lang="ts">
import { useSettingsDepositAccountStore } from '@/stores/settings';
import { defineExpose, ref } from 'vue';

const visibleDialog = ref(false);

const depositAccount = useSettingsDepositAccountStore();
depositAccount.queryAllRows();

const initialData = {
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
    visibleDialog.value = true;
};
const onDelete = (selectedData: any) => {
    depositAccount.deleteConfirm(selectedData);
};
const onEdit = (selectedData: any) => {
    selectedRow.value = selectedData;
    visibleDialog.value = true;
};
const onSave = async () => {
    const ret = await depositAccount.saveRow(selectedRow.value);
    if (ret) {
        visibleDialog.value = false;
    }
};
const onRowSelect = (event: any) => {
    selectedRow.value = event.data;
};

const visible = ref(false);
const toggle = () => {
    visible.value = !visible.value;
};
defineExpose({ toggle });
</script>
