<script setup lang="ts">
import DepositAccountDrawer from '@/components/settings/DepositAccountDrawer.vue';
import GlobalSetting from '@/components/settings/GlobalSetting.vue';
import MaterialFlowDrawer from '@/components/settings/MaterialFlowDrawer.vue';
import WhiteListDrawer from '@/components/settings/WhiteListDrawer.vue';
import { useAuthStore } from '@/utils/auth';
import { useDialog } from 'primevue/usedialog';
import { ref } from 'vue';
const dialog = useDialog();

const drawerDepositAccount = ref();
const drawerMaterialFlow = ref();
const drawerWhiteList = ref();
const loginStore = useAuthStore();

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
        icon: 'pi pi-power-off',
        command: () => {
            loginStore.logout();
        }
    }
]);

const menu = ref();

const toggleMenu = (event: any) => {
    menu.value.toggle(event);
};
</script>

<template>
    <Button type="button" raised icon="pi pi-cog" severity="contrast" @click="toggleMenu" aria-haspopup="true" aria-controls="overlay_menu" class="mr-2" />

    <Menu ref="menu" id="overlay_menu" :model="settingsMenuItems" :popup="true">
        <template #item="{ item, props }">
            <a v-ripple :href="item.url" :target="item.target" v-bind="props.action" aria-hidden="false">
                <span :class="item.icon" />
                <span class="ml-2 p-1">{{ item.label }}</span>
            </a>
        </template>
    </Menu>

    <DepositAccountDrawer ref="drawerDepositAccount" />
    <MaterialFlowDrawer ref="drawerMaterialFlow" />
    <WhiteListDrawer ref="drawerWhiteList" />
</template>

<style scoped>
.main-dropdown-settings {
    padding: 1rem;
}
</style>
