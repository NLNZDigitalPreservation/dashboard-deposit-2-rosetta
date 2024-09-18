<script setup lang="ts">
import { useSettingsWhiteListStore } from '@/stores/settings';
import { type WhiteListUser } from '@/types/deposit';
import { getSelectOption } from '@/utils/helper';
import { defineExpose, onMounted, ref } from 'vue';

const userRoleOptions = ref([
    { name: 'Admin', code: 'admin' },
    { name: 'Normal', code: 'normal' }
]);
const selectedUserRole = ref({ name: 'Normal', code: 'normal' });
const visibleDialogWhiteList = ref(false);

const whiteListStore = useSettingsWhiteListStore();
// whiteListStore.queryAllRows();

const initialData: WhiteListUser = {
    id: undefined,
    whiteUserName: '',
    whiteUserRole: 'normal',
    auditRst: true,
    auditMsg: 'OK'
};
const selectedRow = ref(Object.assign({}, initialData));
const metaKey = ref(true);

const onNew = () => {
    selectedRow.value = Object.assign({}, initialData);
    visibleDialogWhiteList.value = true;
};
const onDelete = (selectedData: any) => {
    whiteListStore.deleteConfirm(selectedData);
};
const onEdit = (selectedData: any) => {
    selectedRow.value = Object.assign({}, selectedData);
    selectedUserRole.value = getSelectOption(userRoleOptions.value, selectedRow.value.whiteUserRole);
    visibleDialogWhiteList.value = true;
};
const onSave = async () => {
    selectedRow.value.whiteUserRole = selectedUserRole.value.code;
    const ret = await whiteListStore.saveRow(selectedRow.value);
    if (ret) {
        visibleDialogWhiteList.value = false;
    }
};
const onRowSelect = (event: any) => {
    selectedRow.value = event.data;
};

onMounted(() => {
    whiteListStore.queryAllRows();
});

const visibleDrawerWhiteList = ref(false);
const toggle = () => {
    visibleDrawerWhiteList.value = !visibleDrawerWhiteList.value;
};
defineExpose({ toggle });
</script>

<template>
    <div v-if="visibleDrawerWhiteList">
        <Toast />
        <ConfirmDialog></ConfirmDialog>
        <Drawer v-model:visible="visibleDrawerWhiteList" header="Deposit Accounts" class="!w-full md:!w-80 lg:!w-[50rem]" position="right">
            <DataTable v-model:selection="whiteListStore.selectedRow" :value="whiteListStore.dataList" :metaKeySelection="metaKey" dataKey="id" @rowSelect="onRowSelect" tableStyle="width:100%" showGridlines sortField="id" :sortOrder="1">
                <Column field="id" header="ID" sortable></Column>
                <Column field="whiteUserName" header="User Name" sortable></Column>
                <Column field="whiteUserRole" header="User Role" sortable></Column>
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

        <Dialog v-model:visible="visibleDialogWhiteList" modal header="Edit Profile" :style="{ width: '55rem' }">
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
                        <InputGroupAddon>User Name</InputGroupAddon>
                        <InputText v-model="selectedRow.whiteUserName" />
                    </FlatInputGroup>
                    <FlatInputGroup class="mt-2 mb-2">
                        <InputGroupAddon>User Role</InputGroupAddon>
                        <Select v-model="selectedUserRole" :options="userRoleOptions" optionLabel="name" placeholder="Select a role" />
                    </FlatInputGroup>
                </Fieldset>

                <Fieldset legend="Health Audit">
                    <Message v-if="selectedRow.auditRst" severity="success" :closable="false">{{ selectedRow.auditMsg }}</Message>
                    <Message v-if="!selectedRow.auditRst" severity="warn" :closable="false">{{ selectedRow.auditMsg }}</Message>
                </Fieldset>
            </Fluid>
            <template #footer>
                <Button label="Save" @click="onSave()" autofocus />
                <Button label="Cancel" outlined @click="visibleDialogWhiteList = false" autofocus />
            </template>
        </Dialog>
    </div>
</template>
