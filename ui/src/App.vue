<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import { useLayout } from '@/layout/composables/layout';
import { usePrimeVue } from 'primevue/config';
import MainView from '@/views/MainView.vue';

const { layoutConfig } = useLayout();
const $primevue = usePrimeVue();

const onChangeTheme = (theme: string, mode: any) => {
    $primevue.changeTheme(layoutConfig.theme.value, theme, 'theme-css', () => {
        layoutConfig.theme.value = theme;
        layoutConfig.darkTheme.value = mode;
    });
};

onMounted(() => {
  onChangeTheme("aura-dark-lime","dark");
});
</script>

<template>
  <div class="app">
     <MainView />
     <DynamicDialog />
  </div>
</template>

<style scoped>
.app {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}
</style>
