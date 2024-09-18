<template>
    <!-- <div class="layout-topbar" style="position: relative; background: linear-gradient(to right, #212529, #32cd32, #212529)"> -->
    <div class="layout-topbar" style="position: relative">
        <div class="layout-topbar-logo-container">
            <router-link to="/" class="layout-topbar-logo">
                <img src="@/assets/natlib-logo-red.png" width="50" height="50" alt="logo" />
                <OverlayBadge value="PRD"> Deposit Dashboard </OverlayBadge>
            </router-link>
        </div>

        <!-- <IconField>
            <InputGroup>
                <InputText placeholder="Filter" style="max-width: 20rem" />
                <Button icon="pi pi-filter" label="Advanced" @click="topbarActions.openSearchDialog" />
            </InputGroup>
        </IconField> -->
        <!-- <div class="p-iconfield p-inputgroup p-inputgroup-fluid mt-2 mb-2">
            <InputText v-model="keywords" type="text" placeholder="Filter" />
            <InputIcon class="pi pi-filter" style="margin-left: -15px" />

            <Button icon="pi pi-search" label="Advanced" @click="topbarActions.openSearchDialog" />
        </div> -->
        <div style="width: 1rem"></div>

        <!-- <Button label="Search" icon="pi pi-search" class="mr-2" @click="topbarActions.openSearchDialog"></Button> -->
        <Button label="Search" icon="pi pi-search" class="mr-2" @click="dlgSearch.show()"></Button>
        <Button @click="topbarActions.onReload()" label="Reload" icon="pi pi-refresh" class="mr-2"></Button>
        <Button @click="topbarActions.onExportSelectedJobs()" label="Export Selected Jobs" icon="pi pi-download" class="mr-2"></Button>
        <Button @click="topbarActions.openRedepositDialog()" label="Redeposit" icon="pi pi-pen-to-square" class="mr-2"></Button>

        <div class="layout-topbar-actions">
            <div class="layout-config-menu">
                <button type="button" class="layout-topbar-action" @click="toggleDarkMode">
                    <i :class="['pi', { 'pi-moon': isDarkTheme, 'pi-sun': !isDarkTheme }]"></i>
                </button>
                <div class="relative">
                    <button
                        v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'animate-scalein', leaveToClass: 'hidden', leaveActiveClass: 'animate-fadeout', hideOnOutsideClick: true }"
                        type="button"
                        class="layout-topbar-action"
                    >
                        <i class="pi pi-palette"></i>
                    </button>
                    <AppConfigurator />
                </div>
            </div>

            <IconField class="mr-2">
                <InputIcon class="pi pi-filter" />
                <InputText v-model="keywords" type="text" placeholder="Filter" />
            </IconField>
            <Button type="button" raised icon="pi pi-cog" severity="contrast" @click="toggleMenu" class="mr-2" />
        </div>
    </div>
    <Menu ref="menu" :model="settingsMenuItems" :popup="true" />

    <div style="width: 100vw; height: calc(100vh - 60px)">
        <router-view />
    </div>

    <DepositJobSearchDialog ref="dlgSearch" />

    <DepositAccountDrawer ref="drawerDepositAccount" />
    <MaterialFlowDrawer ref="drawerMaterialFlow" />
    <WhiteListDrawer ref="drawerWhiteList" />
</template>

<script setup lang="ts">
import DepositJobSearchDialog from '@/components/jobs/DepositJobSearchDialog.vue';
import DepositAccountDrawer from '@/components/settings/DepositAccountDrawer.vue';
import GlobalSetting from '@/components/settings/GlobalSetting.vue';
import MaterialFlowDrawer from '@/components/settings/MaterialFlowDrawer.vue';
import WhiteListDrawer from '@/components/settings/WhiteListDrawer.vue';
import AppConfigurator from '@/layout/AppConfigurator.vue';
import { useLayout } from '@/layout/composables/layout';
import { useJobListDTO } from '@/stores/depositjob';
import { useTopbarActions } from '@/stores/depositjobTopbarActions';
import { useUserProfileStore } from '@/stores/users';
import Menu from 'primevue/menu';
import { useDialog } from 'primevue/usedialog';
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
const dialog = useDialog();

const router = useRouter();
const { onMenuToggle, toggleDarkMode, isDarkTheme } = useLayout();

const keywords = ref();

const jobList = useJobListDTO();

const topbarMenuActive = ref(false);
const topbarMenuClasses = computed(() => {
    return {
        'layout-topbar-menu-mobile-active': topbarMenuActive.value
    };
});
/*  */
const dlgSearch = ref();
const drawerDepositAccount = ref();
const drawerMaterialFlow = ref();
const drawerWhiteList = ref();

const menu = ref();
const settingsMenuItems = ref([
    {
        label: 'Deposit Account Settings',
        icon: 'pi pi-users',
        command: () => {
            // router.push('/setting/deposit-account');
            drawerDepositAccount.value.toggle();
        }
    },
    {
        label: 'Material Flow Settings',
        icon: 'pi pi-objects-column',
        command: () => {
            drawerMaterialFlow.value.toggle();
        }
    },
    {
        label: 'User White List',
        icon: 'pi pi-list-check',
        command: () => {
            drawerWhiteList.value.toggle();
        }
    },
    {
        separator: true
    },
    {
        label: 'Global Setting',
        icon: 'pi pi-globe',
        command: () => {
            const dialogRef = dialog.open(GlobalSetting, {
                props: {
                    header: 'Global Settings',
                    closable: true,
                    style: {
                        width: '50rem'
                    },
                    modal: true
                }
            });
        }
    },
    {
        separator: true
    },
    {
        label: 'Sign out',
        icon: 'pi pi-power-off'
    }
]);

const toggleMenu = (event: any) => {
    menu.value.toggle(event);
};

const token = useUserProfileStore();
const logout = () => {
    token.clear();
};

const topbarActions = useTopbarActions();
</script>
<style></style>
