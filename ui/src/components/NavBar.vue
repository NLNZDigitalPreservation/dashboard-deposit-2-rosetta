<template>
    <div class="layout-topbar">
        <router-link to="/" class="layout-topbar-logo">
            <img src="@/assets/natlib-logo-red.png" alt="logo" />
            <span>Deposit Dashboard</span>
        </router-link>

        <Button label="Search" icon="pi pi-search" class="mr-2 p-3" text></Button>
        <Button label="Reload" icon="pi pi-refresh" class="mr-2 p-3" text></Button>
        <Button label="Export Selected Jobs" icon="pi pi-download" class="mr-2 p-3" text></Button>
        <Button label="Redeposit" icon="pi pi-pen-to-square" class="mr-2 p-3" text></Button>

        <div class="layout-topbar-menu" :class="topbarMenuClasses">
            <Menu ref="menu" :model="settingsMenuItems" :popup="true" />
            <Button type="button" label="Settings" icon="pi pi-chevron-down" iconPos="right" @click="toggleMenu" style="width: auto" text/>

            <!-- <InputSwitch class="my-auto ml-4" :modelValue="layoutConfig.darkTheme.value" @update:modelValue="onDarkModeChange" /> -->
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useUserProfileStore } from '@/stores/users';
import { useLayout } from '@/layout/composables/layout';
import { usePrimeVue } from 'primevue/config';

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
const toggleMenu = (event) => {
    menu.value.toggle(event);
};

const { layoutConfig } = useLayout();
const $primevue = usePrimeVue();

const token = useUserProfileStore();
const logout = ()=>{
    token.clear();
}

const onDarkModeChange = (value: any) => {
    const newThemeName = value ? layoutConfig.theme.value.replace('light', 'dark') : layoutConfig.theme.value.replace('dark', 'light');

    layoutConfig.darkTheme.value = value;
    onChangeTheme(newThemeName, value);
};

const onChangeTheme = (theme: string, mode: any) => {
    $primevue.changeTheme(layoutConfig.theme.value, theme, 'theme-css', () => {
        layoutConfig.theme.value = theme;
        layoutConfig.darkTheme.value = mode;
    });
};

</script>
<style scoped>
.layout-topbar button{
    font-size: 1.3rem;
}

</style>