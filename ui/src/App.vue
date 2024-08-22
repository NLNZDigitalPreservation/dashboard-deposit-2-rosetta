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
</style>
