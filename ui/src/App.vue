<script setup lang="ts">
import { type UseFetchApis, useFetch, useLoginStore } from '@/utils/rest.api';
import { useThemeStore } from '@/utils/themes';
import { useTitle } from '@vueuse/core';
import { onMounted, ref } from 'vue';
import LoginDialog from './components/LoginDialog.vue';

import MainView from './views/MainView.vue';

const rest: UseFetchApis = useFetch();

const loginStore = useLoginStore();
const themeStore = useThemeStore();
const title = useTitle('Dashboard');
const envType = ref('DEV');

onMounted(async () => {
    const sysInfo = await rest.get('/restful/system-info');
    let darkMode = false;
    let colorMode = 'indigo';
    let hostType = 'DEV';
    if (!sysInfo.systemDeployment) {
        hostType = 'DEV';
    } else {
        hostType = sysInfo.systemDeployment.toUpperCase();
    }

    darkMode = true;
    if (hostType === 'PRD') {
        colorMode = 'lime';
    } else if (hostType === 'UAT') {
        colorMode = 'rose';
    } else {
        hostType = 'DEV';
        colorMode = 'blue';
    }

    envType.value = hostType; // bind a ref to the document title
    title.value = `Dashboard (${hostType})`;
    themeStore.toggleTheme(darkMode, colorMode);
});
</script>

<template>
    <LoginDialog v-if="loginStore.visibleLoginWindow" id="login-dialog" />

    <div v-show="!loginStore.visibleLoginWindow">
        <Suspense> <MainView :env-type="envType" /> </Suspense>
    </div>
    <Suspense>
        <DynamicDialog />
    </Suspense>
    <ConfirmDialog></ConfirmDialog>
</template>

<style>
#login-dialog {
    position: fixed;
    z-index: 9999;
}
</style>
