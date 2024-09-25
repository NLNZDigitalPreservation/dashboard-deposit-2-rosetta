<script setup lang="ts">
import LoginDialog from './components/LoginDialog.vue';
import { useLoginStore } from './utils/rest.api';
const loginStore = useLoginStore();
</script>

<template>
    <LoginDialog v-if="loginStore.visibleLoginWindow" id="login-dialog" />

    <div v-show="!loginStore.visibleLoginWindow">
        <router-view />
    </div>

    <DynamicDialog />
</template>

<style>
#login-dialog {
    position: fixed;
    z-index: 9999;
}
</style>
