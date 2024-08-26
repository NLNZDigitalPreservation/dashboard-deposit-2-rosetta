<script setup lang="ts">
import { useJobListDTO } from '@/stores/depositjob';
import { useSettingsDepositAccountStore } from '@/stores/settings';
import { inject, ref } from 'vue';

const depositAccount = useSettingsDepositAccountStore();
depositAccount.fetchData();

const jobList = useJobListDTO();

const dialogRef: any = inject('dialogRef');
const closeDialog = () => {
    dialogRef.value.close();
};

const onRedeposit = () => {
    jobList.redeposit();
    closeDialog();
};

const initialData = {
    id: undefined,
    depositUserInstitute: '',
    depositUserName: '',
    depositUserPassword: '',
    auditRst: true,
    auditMsg: 'OK'
};
const selectedRow = ref(initialData);
const metaKey = ref(true);

const onRowSelect = (event: any) => {
    selectedRow.value = event.data;
};
</script>

<template>
    <Divider style="border-width: 3px; padding: 0; margin: 0" />
    <Fluid>
        <div class="flex flex-col md:flex-row">
            <div class="md:w-2/3" style="height: calc(100vh - 75px)">
                <Toolbar>
                    <template #start> Deposit Account: {{ selectedRow.id }} </template>
                    <template #center> </template>
                    <template #end> <Button label="Save" icon="pi pi-save" class="w-full" raised /></template>
                </Toolbar>

                <div class="p-2" style="overflow-y: auto">
                    <Fieldset legend="Settings">
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
                </div>
            </div>
            <Divider layout="vertical" style="border-width: 3px" />
            <div class="md:w-1/3" style="height: calc(100vh - 75px)">
                <Toolbar>
                    <template #start> <Button label="New" icon="pi pi-plus" raised /></template>
                    <template #center> </template>
                    <template #end> <Button label="Delete" icon="pi pi-trash" raised /></template>
                </Toolbar>

                <DataTable v-model:selection="depositAccount.selectedRow" :value="depositAccount.data" selectionMode="single" :metaKeySelection="metaKey" dataKey="id" @rowSelect="onRowSelect" tableStyle="width:100%" showGridlines>
                    <template #header>
                        <div class="flex flex-wrap items-center justify-between gap-2"></div>
                    </template>
                    <Column selectionMode="single" headerStyle="width: 3rem"></Column>
                    <Column field="id" header="ID"></Column>
                    <Column field="depositUserInstitute" header="Institute"></Column>
                    <Column field="depositUserName" header="User Name"></Column>
                    <Column field="auditMsg" header="Audit Result"> </Column>
                </DataTable>
            </div>
        </div>
    </Fluid>
</template>

<style scoped></style>
