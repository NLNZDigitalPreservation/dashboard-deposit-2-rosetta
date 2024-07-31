<template>
    <div class="grid grid-cols-12 gap-2">
        <label for="name3" class="flex items-center col-span-12 mb-2 md:col-span-3 md:mb-0">{{ key }}</label>
        <div class="col-span-12 md:col-span-9">
            <label> {{ value }}</label>
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    key: string,
    value: string,
}>()
</script>

<style>

</style>