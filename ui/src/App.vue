<script setup lang="ts">
import { useThemeStore } from '@/utils/themes';
import { onMounted } from 'vue';
import LoginDialog from './components/LoginDialog.vue';
import { useLoginStore } from './utils/rest.api';
import MainView from './views/MainView.vue';
const loginStore = useLoginStore();
const themeStore = useThemeStore();
onMounted(() => {
    themeStore.toggleTheme(true, 'lime');
});
</script>

<template>
    <LoginDialog v-if="loginStore.visibleLoginWindow" id="login-dialog" />

    <div v-show="!loginStore.visibleLoginWindow">
        <MainView />
    </div>

    <!-- <div v-show="false">
        <router-view />
    </div> -->

    <DynamicDialog />
</template>

<style>
#login-dialog {
    position: fixed;
    z-index: 9999;
}
</style>
