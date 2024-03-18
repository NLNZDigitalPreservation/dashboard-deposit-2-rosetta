<script setup lang="ts">
import { ref } from "vue";


const menu = ref();
const items = ref([
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
]);

const toggle = (event:any) => {
    menu.value.toggle(event);
};

</script>

<template>
    <Toolbar style="border: 0; border-radius:0; padding: 5px 0 0 0; ">
        <template #start>
            <Avatar image="/natlib-logo-orange.png" style="margin-bottom: 5px; margin-left: 1em;" />
            <h2 style="color: white; margin-bottom: 8px; margin-left: 1em; margin-right: 1em;">Deposit Dashboard</h2>
            
            <div id="toolkit-container-left" class="toolkit-container flex align-items-center gap-2">
                <InputText placeholder="Filter" type="text" class="w-8rem sm:w-auto" />
                <Button icon="pi pi-search" label="&nbsp;&nbsp;Search" />
                <Button icon="pi pi-refresh" label="&nbsp;&nbsp;Reload" />
                <Button icon="pi pi-download" label="&nbsp;&nbsp;Export Selected Jobs" />
                <Button icon="pi pi-file-edit" label="&nbsp;&nbsp;Redeposit" />
            </div>
        </template>

        <template #end>
            <div class="toolkit-container flex align-items-center gap-2">
                <Button type="button" icon="pi pi-cog" label="&nbsp;&nbsp;Settings" @click="toggle" aria-haspopup="true" aria-controls="overlay_menu" link />
                <Menu ref="menu" id="overlay_menu" :model="items" :popup="true">
                    <template #item="{ item, props }">
                        <a v-ripple class="flex align-items-center" v-bind="props.action">
                            <span :class="item.icon" />
                            <span class="ml-2">{{ item.label }}</span>
                            <Badge v-if="item.badge" class="ml-auto" :value="item.badge" />
                            <span v-if="item.shortcut" class="ml-auto border-1 surface-border border-round surface-100 text-xs p-1">{{ item.shortcut }}</span>
                        </a>
                    </template>
                </Menu>
            </div>
        </template>
    </Toolbar>
</template>

<style scoped>
    .toolkit-container > * {    
        margin-bottom: 8px;
        margin-left: 1em;
    }

    .p-button-icon::after {
        content: "&nbsp;";
    }
</style>
