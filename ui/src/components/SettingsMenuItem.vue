<template>
    <Menubar class="main-menu" :model="items">
        <template #item="{ item, props, hasSubmenu, root }">
            <a v-ripple class="flex align-items-center" v-bind="props.action">
                <span :class="item.icon" />
                <span class="ml-2">&nbsp;&nbsp; {{ item.label }}</span>
                <Badge v-if="item.badge" :class="{ 'ml-auto': !root, 'ml-2': root }" :value="item.badge" />
                <span v-if="item.shortcut" class="ml-auto surface-100 text-xs p-1">{{ item.shortcut }}</span>
                <i v-if="hasSubmenu" :class="['pi pi-angle-down', { 'pi-angle-down ml-2': root, 'pi-angle-right ml-auto': !root }]"></i>
            </a>
        </template>        
    </Menubar>
</template>

<style scoped>
    .p-menuitem-content a{
        /* display: inline-block; */
        /* padding: 2em; */
        font-size: 1em;
        
    }

    /* .main-menu a{
        display: inline-block;
        width: 250px;
        margin-right: 10px;
    } */

    .p-submenu-list a{
        display: inline-block;
        width: 250px;
    }

    .p-submenu-list {
        position: fixed;
        bottom: 0;
    }

    .p-menubar {
        border: 0;
    }
</style>

<script setup>
import { ref } from "vue";

const items = ref([    
    {
        label: 'Settings',
        icon: 'pi pi-cog',
        items: [
            {
                label: 'Deposit Account Settings'          
            },
            {
                label: 'Material Flow Settings'
            },
            {
                label: 'User White List'
            },
            {
                separator: true
            },
            {
                label: 'Global Settings'
            },
            {
                separator: true
            },
            {
                label: 'Sign out'
            }
        ]
    }
]);
</script>