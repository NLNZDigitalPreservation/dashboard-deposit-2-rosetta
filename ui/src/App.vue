<script setup lang="ts">
import MainView from '@/views/MainView.vue';
</script>

<template>
    <div class="app">
        <MainView />
        <DynamicDialog />
    </div>
</template>

<style>
.app {
    height: 100vh;
    width: 100vw;
    overflow: hidden;
}
.p-dialog {
    border: 0;
}
.p-dialog-header {
    height: 0px;
    padding: 0px;
}

.p-dialog-content {
    padding: 0px 12px 12px 12px;
    border: 5px solid;
    border-color: rgb(59, 73, 73);
    border-top-right-radius: 12px;
    border-top-left-radius: 12px;
    border-bottom-right-radius: 12px;
    border-bottom-left-radius: 12px;
}
</style>
