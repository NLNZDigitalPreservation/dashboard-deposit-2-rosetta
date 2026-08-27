<script setup lang="ts">
import { useAuthStore } from '@/utils/auth';
import { useUserProfileStore } from '@/utils/users';
import { computed, ref } from 'vue';

const authStore = useAuthStore();

const userProfile = useUserProfileStore();

const displayName = computed(() => {
    const info = userProfile.userInfo;
    return info?.presentation_name || info?.username || info?.email || 'Anonymous';
});

const email = computed(() => {
    const info = userProfile.userInfo;
    return info?.email || info?.username || 'anonymous@dia.govt.nz';
});

const defaultOrganization = 'Te Tari Taiwhenua, Department of In...';
const organization = computed(() => {
    const info = userProfile.userInfo;
    return info?.organization || info?.department || defaultOrganization;
});

const initials = computed(() => {
    const name = displayName.value;
    if (!name) return 'U';
    const parts = name.split(/[@.\s]+/);
    if (parts.length >= 2) {
        return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name[0].toUpperCase();
});

const avatar = await authStore.getAvatar();
const avatarUrl = ref(avatar || null);

const emit = defineEmits(['close']);

const op = ref();
const toggle = (event) => {
    op.value.toggle(event);
};
</script>

<template>
    <button type="button" class="layout-topbar-action" @click="toggle">
        <Avatar v-if="avatarUrl" :image="avatarUrl" shape="circle" size="normal" />
        <i v-else class="pi pi-user"></i>
    </button>

    <!-- viridian color: #6aab95 -->
    <Popover ref="op" class="p-4" style="background-color: var(--surface-ground)">
        <div class="flex justify-start items-center gap-4 w-full">
            <div>
                <Avatar v-if="avatarUrl" :image="avatarUrl" shape="circle" size="xlarge" class="user-profile-avatar-xxl" />
                <Avatar v-else :label="initials" shape="circle" size="xlarge" class="user-profile-avatar-xxl" />
            </div>
            <div class="flex flex-col justify-between items-end gap-4">
                <span class="text-2xl font-bold">{{ displayName }}</span>
                <span class="text">{{ email }}</span>
            </div>
        </div>

        <Divider class="my-0" />
        <div class="flex justify-between items-center gap-4 w-full">
            <span class="font-semibold">{{ organization }}</span>
            <Button class="user-profile-signout-btn" label="Sign out" @click="authStore.logout" severity="danger" />
        </div>
    </Popover>
</template>

<style scoped>
.layout-topbar-action :deep(.p-avatar) {
    cursor: pointer;
}

.user-profile-avatar-xxl {
    width: 7rem;
    height: 7rem;
}

.user-profile-avatar-xxl :deep(.p-avatar-label) {
    font-size: 6rem;
}
</style>
