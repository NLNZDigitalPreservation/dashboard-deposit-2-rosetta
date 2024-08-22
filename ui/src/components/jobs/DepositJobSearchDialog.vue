<script setup lang="ts">
import { useMaterialFlows } from '@/stores/materialflows';
import { inject, ref } from 'vue';
// import { type UseFetchApis, useFetch } from "@/utils/rest.api";
import { searchConditions, useJobListDTO } from '@/stores/depositjob';

const jobList = useJobListDTO();

const dialogRef: any = inject('dialogRef');
// const params = dialogRef.value.data;
// const formatedFileSize = ref();

const materialflows = useMaterialFlows();
materialflows.fetchAllData();

const stages = ref([
    { name: 'INGEST', code: 'INGEST' },
    { name: 'DEPOSIT', code: 'DEPOSIT' },
    { name: 'FINALIZE', code: 'FINALIZE' },
    { name: 'FINISHED', code: 'FINISHED' }
]);

const states = ref([
    { name: 'INITIALED', code: 'INITIALED' },
    { name: 'RUNNING', code: 'RUNNING' },
    { name: 'PAUSED', code: 'PAUSED' },
    { name: 'SUCCEED', code: 'SUCCEED' },
    { name: 'FAILED', code: 'FAILED' },
    { name: 'CANCELED', code: 'CANCELED' }
]);

const closeDialog = () => {
    dialogRef.value.close();
};

const onReset = () => {
    searchConditions.fromDate = undefined;
    searchConditions.toDate = undefined;
    searchConditions.selectedData = [];
    searchConditions.selectedStages = [];
    searchConditions.selectedStates = [];
};

const onSearch = () => {
    const req = {
        dtStart: 0,
        dtEnd: Date.now().valueOf(),
        flowIds: [] as string[],
        stages: [] as string[],
        states: [] as string[]
    };

    if (searchConditions.fromDate) {
        req.dtStart = searchConditions.fromDate.valueOf();
    }

    if (searchConditions.toDate) {
        req.dtEnd = searchConditions.toDate.valueOf();
    }

    searchConditions.selectedData.forEach((flow: any) => {
        req.flowIds.push(flow.id);
    });

    searchConditions.selectedStages.forEach((stage: any) => {
        req.stages.push(stage.code);
    });

    searchConditions.selectedStates.forEach((state: any) => {
        req.states.push(state.code);
    });

    jobList.searchData(req);
    closeDialog();
};
</script>

<template>
    <div class="font-semibold">Latested Update Datetime</div>
    <InputGroup class="mt-2 mb-2">
        <InputGroupAddon>From</InputGroupAddon>
        <DatePicker v-model="searchConditions.fromDate" />
        <InputGroupAddon>To</InputGroupAddon>
        <DatePicker v-model="searchConditions.toDate" />
    </InputGroup>

    <div class="font-semibold">Material Flow</div>
    <DataTable v-model:selection="searchConditions.selectedData" :value="materialflows.data" dataKey="id" scrollable scrollHeight="25rem" tableStyle="min-width: 60rem">
        <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
        <Column field="id" header="ID"></Column>
        <Column field="materialFlowName" header="Material Flow"></Column>
        <Column field="producerName" header="Producer"></Column>
        <Column field="enabled" header="Available"></Column>
    </DataTable>

    <div class="flex flex-wrap gap-4">
        <div class="flex flex-col grow basis-0 gap-2">
            <label for="name2">Stage</label>
            <MultiSelect v-model="searchConditions.selectedStages" :options="stages" optionLabel="name" placeholder="Select stages" :filter="false"></MultiSelect>
        </div>
        <div class="flex flex-col grow basis-0 gap-2">
            <label for="email2">State</label>
            <MultiSelect v-model="searchConditions.selectedStates" :options="states" optionLabel="name" placeholder="Select states" :filter="false"></MultiSelect>
        </div>
    </div>

    <Toolbar style="border: 0">
        <template #start>
            <div class="flex justify-center flex-wrap gap-4 mt-4" style="justify-content: flex-start">
                <Button type="button" label="Reset" @click="onReset()"></Button>
            </div>
        </template>

        <template #end>
            <div class="flex justify-center flex-wrap gap-4 mt-4" style="justify-content: flex-end">
                <Button type="button" label="Search" @click="onSearch()" autofocus></Button>
                <Button type="button" label="Close" @click="closeDialog()" severity="secondary"></Button>
            </div>
        </template>
    </Toolbar>
</template>

<style scoped></style>
