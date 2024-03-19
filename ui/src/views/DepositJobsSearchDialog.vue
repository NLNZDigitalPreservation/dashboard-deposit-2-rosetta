<script setup lang="ts">
import { ref, computed } from 'vue';
import { reactive } from 'vue';
import {status} from "../exchange";

const props = defineProps(["visible"]);
let isVisible=props.visible;



</script>

<template>
    <Dialog v-model:visible="status.visible" modal header="Search" :style="{ width: '50rem' }">
        <span class="p-text-secondary block mb-5">Latested Update Datetime:</span>
        <!-- <InputGroup>
            <div>From</div>
            <Calendar v-model="icondisplay" showIcon iconDisplay="input" />
            <div>To</div>
            <Calendar v-model="icondisplay" showIcon iconDisplay="input" />
        </InputGroup>
        
        <span class="p-text-secondary block mb-5">Material Flow:</span>
        <MultiSelect v-model="selectedCities" :options="groupedCities" optionLabel="label" optionGroupLabel="label" optionGroupChildren="items" display="chip" placeholder="Select Cities" class="w-full md:w-20rem">
            <template #optiongroup="slotProps">
                <div class="flex align-items-center">
                    <img :alt="slotProps.option.label" src="https://primefaces.org/cdn/primevue/images/flag/flag_placeholder.png" :class="`flag flag-${slotProps.option.code.toLowerCase()} mr-2`" style="width: 18px" />
                    <div>{{ slotProps.option.label }}</div>
                </div>
            </template>
        </MultiSelect>

        <span class="p-text-secondary block mb-5">Stage:</span>
        <MultiSelect v-model="selectedCities" :options="cities" optionLabel="name" placeholder="Select Cities" :maxSelectedLabels="3" class="w-full md:w-20rem" />

        <span class="p-text-secondary block mb-5">State:</span>
        <MultiSelect v-model="selectedCities" :options="cities" optionLabel="name" placeholder="Select Cities" :maxSelectedLabels="3" class="w-full md:w-20rem" /> -->

        <div class="flex justify-content-end gap-2">
            <Button type="button" label="Cancel" severity="secondary" @click="status.visible = false"></Button>
            <Button type="button" label="Save" @click="status.visible = false"></Button>
        </div>
    </Dialog>
</template>
