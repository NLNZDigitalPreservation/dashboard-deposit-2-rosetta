<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useLayout } from '@/layout/composables/layout';
import { useRouter } from 'vue-router';

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

const toggle = (event) => {
    menu.value.toggle(event);
};

const { layoutConfig, onMenuToggle } = useLayout();

const outsideClickListener = ref(null);
const topbarMenuActive = ref(false);
const router = useRouter();

onMounted(() => {
    bindOutsideClickListener();
});

onBeforeUnmount(() => {
    unbindOutsideClickListener();
});

const logoUrl = computed(() => {
    return `/layout/images/${layoutConfig.darkTheme.value ? 'logo-white' : 'logo-dark'}.svg`;
});

const onTopBarMenuButton = () => {
    topbarMenuActive.value = !topbarMenuActive.value;
};
const onSettingsClick = () => {
    topbarMenuActive.value = false;
    router.push('/documentation');
};
const topbarMenuClasses = computed(() => {
    return {
        'layout-topbar-menu-mobile-active': topbarMenuActive.value
    };
});

const bindOutsideClickListener = () => {
    if (!outsideClickListener.value) {
        outsideClickListener.value = (event) => {
            if (isOutsideClicked(event)) {
                topbarMenuActive.value = false;
            }
        };
        document.addEventListener('click', outsideClickListener.value);
    }
};
const unbindOutsideClickListener = () => {
    if (outsideClickListener.value) {
        document.removeEventListener('click', outsideClickListener);
        outsideClickListener.value = null;
    }
};
const isOutsideClicked = (event) => {
    if (!topbarMenuActive.value) return;

    const sidebarEl = document.querySelector('.layout-topbar-menu');
    const topbarEl = document.querySelector('.layout-topbar-menu-button');

    return !(sidebarEl.isSameNode(event.target) || sidebarEl.contains(event.target) || topbarEl.isSameNode(event.target) || topbarEl.contains(event.target));
};
</script>

<template>
    <div class="layout-topbar">
        <router-link to="/" class="layout-topbar-logo">
            <img :src="logoUrl" alt="logo" />
            <span>SAKAI</span>
        </router-link>

        <InputText placeholder="Filter" type="text" class="w-8rem sm:w-auto" />

        <button class="p-link layout-menu-button layout-topbar-button" label="Toggle" @click="onMenuToggle()">
            <i class="pi pi-search"></i>
            <span>Search</span>
        </button>
        <button class="p-link layout-menu-button layout-topbar-button" label="Toggle" @click="onMenuToggle()">
            <i class="pi pi-refresh"></i>
            <span>Reload</span>
        </button>
        <button class="p-link layout-menu-button layout-topbar-button" label="Toggle" @click="onMenuToggle()">
            <i class="pi pi-download"></i>
            <span>Export</span>
        </button>
        <button class="p-link layout-menu-button layout-topbar-button" label="Toggle" @click="onMenuToggle()">
            <i class="pi pi-file-edit"></i>
            <span>Redeposit</span>
        </button>

        <div class="layout-topbar-menu" :class="topbarMenuClasses">
            <button class="p-link layout-menu-button layout-topbar-button" label="Toggle" @click="toggle">
                <i class="pi pi-cog"></i>
                <span>Settings</span>
            </button>
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
    </div>
</template>

<style lang="scss" scoped></style>
