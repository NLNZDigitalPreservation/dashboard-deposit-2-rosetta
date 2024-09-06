<script setup lang="ts">
import { computed, defineProps, ref } from 'vue';
const props = defineProps<{
    dataList?: any;
}>();

interface Producer {
    id: string;
    name: string;
    isChecked: false;
}

const selectedData = ref([]);
const expandedRowGroups = ref([]);

const datesets: any = {};
for (let flow of props.dataList) {
    datesets[flow.producerId] = {
        isChecked: false,
        isIndeteminate: false
    };
}
const producerSelect = ref(datesets);

// const isIndeteminate = ref(false);
const isIndeteminate = computed(() => {
    let selectedNum = 0;
    for (let flow of flows.value) {
        if (flow.isChecked) {
            selectedNum++;
        }
    }

    if (selectedNum == 0) {
        producerSelect.value = false;
    }
    if (selectedNum == flows.value.length) {
        producerSelect.value = true;
    }

    if (selectedNum == 0 || selectedNum == flows.value.length) {
        return false;
    }

    return true;
});
const onToggle = (isChecked: boolean, producerId: string) => {
    const producer: any = producerSelect.value[producerId];
    if (producer) {
        producer.isChecked = isChecked;
    }
    if (isChecked) {
    }
};
</script>

<template>
    <DataTable
        v-model:expandedRowGroups="expandedRowGroups"
        v-model:selection="selectedData"
        :value="dataList"
        dataKey="id"
        rowGroupMode="rowspan"
        groupRowsBy="producerName"
        sortMode="single"
        sortField="producerName"
        :sortOrder="1"
        showGridline
        resizableColumns
        columnResizeMode="expand"
    >
        <Column field="id" header="ID"></Column>

        <Column field="producerName" header="Producer">
            <template #body="slotProps">
                <div class="flex items-center gap-2">
                    <Checkbox
                        @update:modelValue="onToggle($event, slotProps.data.producerId)"
                        v-model="producerSelect[slotProps.data.producerId].isChecked"
                        :inputId="slotProps.data.producerName"
                        name="Producer"
                        :binary="true"
                        :indeterminate="producerSelect[slotProps.data.producerId].isIndeteminate"
                    />
                    <label :for="slotProps.data.producerName" class="font-bold"> <i class="pi pi-list"></i> {{ slotProps.data.producerName }} </label>
                </div>
            </template>
        </Column>
        <Column selectionMode="multiple" headerStyle="width: 3rem;"></Column>
        <Column field="materialFlowName" header="Material Flow">
            <template #body="slotProps">
                {{ slotProps.data.materialFlowName }}
                <span v-if="!slotProps.data.enabled" style="background-color: #6c757d; color: #fff; border-radius: 6px; margin-left: 6px; font-size: 0.7rem; font-weight: lighter">Disabled</span>
            </template>
        </Column>
    </DataTable>
</template>
