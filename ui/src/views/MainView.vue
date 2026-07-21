<script setup lang="ts">
import DepositJobListDataTable from '@/components/jobs/DepositJobListDataTable.vue';
import DepositJobSearchDialog from '@/components/jobs/DepositJobSearchDialog.vue';
import { useTopbarActions } from '@/stores/depositjobTopbarActions';
import { useSystemInfoStore } from '@/utils/system.info.store';
import MainDropdownSettings from '@/views/MainDropdownSettings.vue';
import { computed, ref } from 'vue';

const systemInfoStore = useSystemInfoStore();
const envType = computed(() => {
    const sysInfo = systemInfoStore.data;
    if (!sysInfo.appDeployment) {
        return 'DEV';
    } else {
        return sysInfo.appDeployment.toUpperCase();
    }
});

const keywords = ref();
const dlgSearch = ref();
const topbarActions = useTopbarActions();
</script>

<template>
    <Toast position="bottom-left"></Toast>
    <!-- <div class="layout-topbar" style="position: relative; background: linear-gradient(to right, #212529, #32cd32, #212529)"> -->
    <div class="layout-topbar" style="position: relative">
        <div class="layout-topbar-logo-container">
            <router-link to="/" class="layout-topbar-logo">
                <img src="@/assets/natlib-logo-red.png" width="50" height="50" alt="logo" />
                <OverlayBadge :value="envType"> Deposit Dashboard </OverlayBadge>
            </router-link>
        </div>

        <div style="width: 1rem"></div>

        <Button label="Search" icon="pi pi-search" class="mr-2" @click="dlgSearch.show()"></Button>
        <Button @click="topbarActions.onReload()" label="Reload" icon="pi pi-refresh" class="mr-2"></Button>
        <Button @click="topbarActions.onExportSelectedJobs()" label="Export Selected Jobs" icon="pi pi-download" class="mr-2"></Button>
        <Button @click="topbarActions.openRedepositDialog()" label="Redeposit" icon="pi pi-pen-to-square" class="mr-2"></Button>

        <div class="layout-topbar-actions">
            <IconField class="mr-2">
                <InputIcon class="pi pi-filter" />
                <InputText v-model="keywords" type="text" placeholder="Filter" />
            </IconField>
            <MainDropdownSettings />
        </div>
    </div>
    <div style="width: 100vw; height: calc(100vh - 60px)">
        <DepositJobListDataTable />
    </div>

    <DepositJobSearchDialog ref="dlgSearch" />
</template>
