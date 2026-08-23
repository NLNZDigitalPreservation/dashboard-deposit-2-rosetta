<script setup lang="ts">
import router from '@/router';
import { useMsalStore } from '@/utils/msal';
import { onMounted } from 'vue';

const msalStore = useMsalStore();

const getUrlParameter = (param: string): string | null => {
    const searchParams = new URLSearchParams(window.location.search);
    return searchParams.get(param);
};

onMounted(async () => {
    const error = getUrlParameter('error');
    // Handle access_denied specifically
    if (error === 'access_denied') {
        await msalStore.requireLogin(true);
    } else {
        await msalStore.login();
        await router.push('/');
    }
});
</script>

<template>
    <span>Checking</span>
</template>
