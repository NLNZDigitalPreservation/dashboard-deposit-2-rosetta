<script setup lang="ts">
import { useMaterialFlows } from '@/stores/materialflows';
import { inject, ref } from 'vue';

const dialogRef: any = inject('dialogRef');
const params = dialogRef.value.data;
const formatedFileSize = ref();

const fromDate = ref();
const toDate = ref();

const materialflows = useMaterialFlows();
materialflows.fetchAllData();

const stages = ref([
    { name: 'INGEST', code: 'INGEST' },
    { name: 'DEPOSIT', code: 'DEPOSIT' },
    { name: 'FINALIZE', code: 'FINALIZE' },
    { name: 'FINISHED', code: 'FINISHED' }
]);
const selectedStages = ref([]);

const states = ref([
    { name: 'INITIALED', code: 'INITIALED' },
    { name: 'RUNNING', code: 'RUNNING' },
    { name: 'PAUSED', code: 'PAUSED' },
    { name: 'SUCCEED', code: 'SUCCEED' },
    { name: 'FAILED', code: 'FAILED' },
    { name: 'CANCELED', code: 'CANCELED' }
]);
const selectedStates = ref([]);

const closeDialog = (event: any) => {
    dialogRef.value.close(event);
};
</script>

<template>
    <div class="font-semibold">Latested Update Datetime</div>
    <InputGroup class="mt-2 mb-2">
        <InputGroupAddon>From</InputGroupAddon>
        <DatePicker v-model="fromDate" />
        <InputGroupAddon>To</InputGroupAddon>
        <DatePicker v-model="toDate" />
    </InputGroup>

    <div class="font-semibold">Material Flow</div>
    <DataTable v-model:selection="materialflows.selectedData" :value="materialflows.data" dataKey="id" scrollable scrollHeight="25rem" tableStyle="min-width: 60rem">
        <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
        <Column field="id" header="ID"></Column>
        <Column field="materialFlowName" header="Material Flow"></Column>
        <Column field="producerName" header="Producer"></Column>
        <Column field="enabled" header="Available"></Column>
    </DataTable>

    <div class="flex flex-wrap gap-4">
        <div class="flex flex-col grow basis-0 gap-2">
            <label for="name2">Stage</label>
            <MultiSelect v-model="selectedStages" :options="stages" optionLabel="name" placeholder="Select stages" :filter="false"></MultiSelect>
        </div>
        <div class="flex flex-col grow basis-0 gap-2">
            <label for="email2">State</label>
            <MultiSelect v-model="selectedStates" :options="states" optionLabel="name" placeholder="Select states" :filter="false"></MultiSelect>
        </div>
    </div>

    <Divider />

    <div class="flex justify-center flex-wrap gap-4 mt-4" style="justify-content: flex-end">
        <Button type="button" label="Search" @click="closeDialog({ buttonType: 'Confirm' })" autofocus></Button>
        <Button type="button" label="Close" @click="closeDialog({ buttonType: 'Cancel' })" severity="secondary"></Button>
    </div>
</template>

<style scoped></style>
