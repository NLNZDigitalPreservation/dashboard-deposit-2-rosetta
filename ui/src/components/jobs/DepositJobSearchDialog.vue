<script setup lang="ts">
import { searchConditions, useJobListDTO } from '@/stores/depositjob';
import { useSettingsMaterialFlowStore } from '@/stores/settings';
import { computed, defineExpose, inject, ref } from 'vue';

const visible = ref(false);
const jobList = useJobListDTO();

const dialogRef: any = inject('dialogRef');

const materialflows = useSettingsMaterialFlowStore();
materialflows.queryAllRows();

const mappedMaterialFlows = computed(() => {
    const datamap = new Map();
    for (let flow of materialflows.dataList) {
        if (!datamap.has(flow.producerId)) {
            datamap.set(flow.producerId, {
                label: flow.producerName,
                items: []
            });
        }
        const flowGroup = datamap.get(flow.producerId);
        flowGroup.items.push(flow);
    }
    const values = datamap.values();
    return values;
});

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

const isIndeteminate = computed(() => {
    return true;
});
const producersSelect = ref(false);
const show = () => {
    visible.value = true;
};

defineExpose({ show });
</script>

<template>
    <Dialog v-model:visible="visible" modal header="Advanced Filter" :style="{ width: '75rem' }">
        <div class="font-semibold">Latested Update Datetime</div>
        <InputGroup class="mt-2 mb-2">
            <InputGroupAddon>From</InputGroupAddon>
            <DatePicker v-model="searchConditions.fromDate" />
            <InputGroupAddon>To</InputGroupAddon>
            <DatePicker v-model="searchConditions.toDate" />
        </InputGroup>
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

        <Fieldset legend="Material Flow">
            <MaterialFlowSelectGroup :dataList="materialflows.dataList" />
        </Fieldset>

        <template #footer>
            <Toolbar style="width: 100%; border: 0">
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
    </Dialog>
</template>

<style scoped></style>
