<script setup lang="ts">
import { useSystemInfoStore } from '@/utils/system.info.store';
import { useThemeStore } from '@/utils/themes';
import { useTitle } from '@vueuse/core';
import { onMounted, ref } from 'vue';

const systemInfoStore = useSystemInfoStore();

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
        <router-view />
    </Suspense>
    <DynamicDialog />
    <ConfirmDialog />

    <Toast group="toast-info" position="bottom-left">
        <template #message="slotProps">
            <div class="flex flex-col items-start flex-auto">
                <div class="flex items-center gap-2">
                    <i class="pi pi-info-circle" />
                    <span class="font-bold">{{ slotProps.message.summary }}</span>
                </div>
                <div class="p-2">{{ slotProps.message.detail }}</div>
            </div>
        </template>
    </Toast>
    <Toast group="toast-error" position="bottom-left">
        <template #message="slotProps">
            <div class="flex flex-col items-start flex-auto">
                <div class="flex items-center gap-2">
                    <i class="pi pi-exclamation-triangle" />
                    <span class="font-bold">{{ slotProps.message.summary }}</span>
                </div>
                <div class="font-medium text-lg my-4">{{ slotProps.message.detail }}</div>
            </div>
        </template>
    </Toast>
    <ConfirmDialog group="dlg-error">
        <template #container="{ message, acceptCallback, rejectCallback }">
            <Panel>
                <template #header>
                    <div class="flex items-center gap-2">
                        <span class="font-bold">{{ message.header }}</span>
                    </div>
                </template>
                <template #footer>
                    <div class="flex flex-col items-center justify-center w-full">
                        <Divider />
                        <Button label="OK" @click="acceptCallback" class="w-32" severity="secondary"></Button>
                    </div>
                </template>
                <template #icons>
                    <Button icon="pi pi-times" severity="secondary" rounded text @click="rejectCallback" />
                </template>
                <div class="flex items-center gap-2">
                    <!-- <IconWarning style="width: 30px; height: 30px" /> -->
                    <i class="pi pi-exclamation-triangle" style="color: var(--p-toast-error-color); font-size: 3rem"></i>
                    <div style="min-width: 30rem; max-width: 50rem">
                        <p class="m-0">{{ message.message }}</p>
                    </div>
                </div>
            </Panel>
        </template>
    </ConfirmDialog>
</template>
