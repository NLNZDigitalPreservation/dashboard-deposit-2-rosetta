<template>
    <div class="layout-topbar">
        <router-link to="/" class="layout-topbar-logo">
            <img src="@/assets/natlib-logo-red.png" alt="logo" />
            <span class="mb-2" v-badge="'PRD'">Deposit Dashboard</span>
        </router-link>

        <IconField iconPosition="left" class="mr-2">
            <InputText v-model="keywords" type="text" placeholder="Filter" />
            <InputIcon class="pi pi-filter" />
        </IconField>

        <Button label="Search" icon="pi pi-search" class="mr-2"></Button>
        <Button label="Reload" icon="pi pi-refresh" class="mr-2"></Button>
        <Button label="Export Selected Jobs" icon="pi pi-download" class="mr-2"></Button>
        <Button label="Redeposit" icon="pi pi-pen-to-square" class="mr-2"></Button>

        <div class="layout-topbar-menu" :class="topbarMenuClasses">
            <Menu ref="menu" :model="settingsMenuItems" :popup="true" />
            <Button type="button" label="Settings" icon="pi pi-chevron-down" iconPos="right" @click="toggleMenu" class="mr-2"/>
        </div>
    </div>
    <!-- <Divider /> -->
    <div class="main-table">
        <DepositJobList></DepositJobList>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useUserProfileStore } from '@/stores/users';
import DepositJobList from './DepositJobList.vue';
import {depositJobList, keywords} from '@/stores/depositjob';

const topbarMenuActive = ref(false);
const topbarMenuClasses = computed(() => {
    return {
        'layout-topbar-menu-mobile-active': topbarMenuActive.value
    };
});

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

const menu = ref(null);
const toggleMenu = (event:any) => {
    menu.value.toggle(event);
};


const token = useUserProfileStore();
const logout = ()=>{
    token.clear();
}

</script>
<style scoped>
button{
    font-size: 1rem;
}
.main-table{
    position: absolute;
    top: 80px;
    height: calc(100vh - 80px);
    width: 100vw;
    overflow: hidden;
}
</style>