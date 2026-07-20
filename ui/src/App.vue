<script setup lang="ts">
import { useSystemInfoStore } from '@/utils/system.info.store';
import { useThemeStore } from '@/utils/themes';
import { useUserProfileStore } from '@/utils/users';
import { useTitle } from '@vueuse/core';
import { onMounted, ref } from 'vue';
import MainView from './views/MainView.vue';

const systemInfoStore = useSystemInfoStore();
const userProfileStore = useUserProfileStore();

const themeStore = useThemeStore();
const title = useTitle('Dashboard');
const envType = ref('DEV');

onMounted(async () => {
    const sysInfo = systemInfoStore.data;

    let darkMode = false;
    let colorMode = 'indigo';
    let hostType = 'DEV';
    if (!sysInfo.appDeployment) {
        hostType = 'DEV';
    } else {
        hostType = sysInfo.appDeployment.toUpperCase();
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
    <Suspense>
        <MainView :env-type="envType" />
    </Suspense>
    <Suspense>
        <DynamicDialog />
    </Suspense>
    <Suspense>
        <ConfirmDialog />
    </Suspense>
</template>

<style>
#login-dialog {
    position: fixed;
    z-index: 9999;
}
</style>
