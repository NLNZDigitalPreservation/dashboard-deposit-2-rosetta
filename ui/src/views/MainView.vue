<template>
    <!-- <AppTopbar></AppTopbar> -->
    <div class="layout-topbar">
        <div class="layout-topbar-logo-container">
            <router-link to="/" class="layout-topbar-logo">
                <img src="@/assets/natlib-logo-red.png" width="50" height="50" alt="logo" />
                <OverlayBadge value="PRD"> Deposit Dashboard </OverlayBadge>
            </router-link>
        </div>

        <IconField iconPosition="left" class="mr-2">
            <InputText v-model="keywords" type="text" placeholder="Filter" />
            <InputIcon class="pi pi-filter" />
        </IconField>

        <Button label="Search" icon="pi pi-search" class="mr-2"></Button>
        <Button label="Reload" icon="pi pi-refresh" class="mr-2"></Button>
        <Button label="Export Selected Jobs" icon="pi pi-download" class="mr-2"></Button>
        <Button label="Redeposit" icon="pi pi-pen-to-square" class="mr-2"></Button>

        <div class="layout-topbar-actions">
            <div class="layout-config-menu">
                <button type="button" class="layout-topbar-action" @click="toggleDarkMode">
                    <i :class="['pi', { 'pi-moon': isDarkTheme, 'pi-sun': !isDarkTheme }]"></i>
                </button>
                <div class="relative">
                    <button
                        v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'animate-scalein', leaveToClass: 'hidden', leaveActiveClass: 'animate-fadeout', hideOnOutsideClick: true }"
                        type="button"
                        class="layout-topbar-action layout-topbar-action-highlight"
                    >
                        <i class="pi pi-palette"></i>
                    </button>
                    <AppConfigurator />
                </div>
            </div>

            <div class="layout-topbar-menu" :class="topbarMenuClasses">
                <Menu ref="menu" :model="settingsMenuItems" :popup="true" />
                <Button type="button" raised rounded icon="pi pi-cog" severity="contrast" @click="toggleMenu" class="mr-2" />
            </div>
        </div>
    </div>
    <!-- <Divider /> -->
    <div class="main-table">
        <DepositJobList></DepositJobList>
    </div>
</template>

<script setup lang="ts">
import AppConfigurator from '@/layout/AppConfigurator.vue';
import { useLayout } from '@/layout/composables/layout';
import { keywords } from '@/stores/depositjob';
import { useUserProfileStore } from '@/stores/users';
import Menu from 'primevue/menu';
import { computed, ref } from 'vue';
import DepositJobList from './DepositJobList.vue';

const { onMenuToggle, toggleDarkMode, isDarkTheme } = useLayout();

const topbarMenuActive = ref(false);
const topbarMenuClasses = computed(() => {
    return {
        'layout-topbar-menu-mobile-active': topbarMenuActive.value
    };
});
/*  */
const menu = ref();
const settingsMenuItems = ref([
    {
        label: 'Deposit Account Settings',
        icon: 'pi pi-users'
    },
    {
        label: 'Material Flow Settings',
        icon: 'pi pi-objects-column'
    },
    {
        label: 'User White List',
        icon: 'pi pi-list-check'
    },
    {
        separator: true
    },
    {
        label: 'Global Setting',
        icon: 'pi pi-globe'
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
</script>
<style scoped>
button {
    font-size: 1rem;
}
.main-table {
    position: absolute;
    top: 55px;
    height: calc(100vh - 60px);
    width: 100vw;
    overflow: hidden;
    border-top: 1px solid;
}
</style>
