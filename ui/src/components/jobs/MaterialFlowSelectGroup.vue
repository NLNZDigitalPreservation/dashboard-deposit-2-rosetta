<script setup lang="ts">
import { computed, defineProps, ref } from 'vue';
const props = defineProps<{
    flowGroup?: any;
}>();

const producerSelect = ref(false);
const flows = ref([] as any[]);
const datesets = [];
for (let flow of props.flowGroup.items) {
    const item = Object.assign(
        {
            isChecked: false
        },
        flow
    );
    datesets.push(item);
}
flows.value = datesets;
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
const onToggle = (event: any) => {
    flows.value.forEach((item: any) => {
        item.isChecked = event;
    });
};
</script>

<template>
    <div class="flex flex-col gap-1 ml-10">
        <div class="field font-semibold text-l">
            <Checkbox @update:modelValue="onToggle" v-model="producerSelect" :inputId="flowGroup.label" name="Producer" :binary="true" :indeterminate="isIndeteminate" class="mb-1" />
            <label :for="flowGroup.label" class="ml-2">Producer: {{ flowGroup.label }} </label>
        </div>

        <div v-for="flow in flows" class="field ml-5">
            <Checkbox v-model="flow.isChecked" :inputId="flow.materialFlowId" name="MaterialFlow" :binary="true" />
            <label :for="flow.materialFlowId" class="ml-2">
                {{ flow.materialFlowName }}
                <span v-if="!flow.enabled" style="background-color: #f5edeb; color: #1a2551; border-radius: 6px; margin-left: 6px">Disabled</span>
            </label>
        </div>
    </div>
</template>
