<script setup lang="ts">
import LoginDialog from './components/LoginDialog.vue';
import { useLoginStore } from './utils/rest.api';
const loginStore = useLoginStore();
</script>

<template>
    <router-view />
    <DynamicDialog />
    <Drawer v-model:visible="loginStore.visibleLoginWindow" position="full">
        <template #container="{ closeCallback }">
            <LoginDialog />
        </template>
    </Drawer>
</template>
