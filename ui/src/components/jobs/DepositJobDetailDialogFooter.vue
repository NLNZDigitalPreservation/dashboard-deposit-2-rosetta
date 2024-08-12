<template>
    <div class="flex justify-center flex-wrap gap-4 mt-4">
        <Button type="button" label="OK" @click="closeDialog({ buttonType: 'Cancel' })" autofocus></Button>
        <Button type="button" label="Close" @click="closeDialog({ buttonType: 'Cancel' })" severity="secondary"></Button>
    </div>
</template>

<script setup>
import { inject } from "vue";

const dialogRef = inject("dialogRef");

const closeDialog = (e) => {
    dialogRef.value.close(e);
};
</script>