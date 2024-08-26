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

const onRowSelect = (event: any) => {
    selectedRow.value = event.data;
};
</script>

<template>
    <Splitter style="width: 100%; height: 100%">
        <SplitterPanel class="flex items-center justify-center" :size="65" :minSize="10">
            <Card style="width: 25rem; overflow: hidden">
                <template #title>Deposit Account: {{ selectedRow.id }}</template>
                <template #content>
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
                </template>
                <template #footer>
                    <div class="flex gap-4 mt-1">
                        <Button label="Save" class="w-full" raised />
                    </div>
                </template>
            </Card>
        </SplitterPanel>
        <SplitterPanel class="flex items-center justify-center" :size="35">
            <DataTable v-model:selection="depositAccount.selectedRow" :value="depositAccount.data" dataKey="id" @rowSelect="onRowSelect" tableStyle="width:100%">
                <template #header>
                    <div class="flex flex-wrap items-center justify-between gap-2">
                        <Button label="New" icon="pi pi-plus" raised />
                        <Button label="Delete" icon="pi pi-delete" raised />
                    </div>
                </template>
                <Column selectionMode="single" headerStyle="width: 3rem"></Column>
                <Column field="id" header="ID"></Column>
                <Column field="depositUserInstitute" header="Institute"></Column>
                <Column field="depositUserName" header="User Name"></Column>
                <Column field="auditRst" header="Audit Result">
                    <template #body="slotProps">
                        {{ slotProps.data.auditRst + ':' + slotProps.data.auditMsg }}
                    </template>
                </Column>
            </DataTable>
        </SplitterPanel>
    </Splitter>
</template>

<style scoped></style>
