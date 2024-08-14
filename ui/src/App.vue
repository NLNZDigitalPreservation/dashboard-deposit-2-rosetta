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
  onChangeTheme("aura-dark-cyan","dark");
});
</script>

<template>
  <div class="app">
     <MainView />
     <DynamicDialog />
  </div>
</template>

<style>
.app {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}
.p-dialog{
  border: 0;
}
.p-dialog-header {
  height: 0px;
  padding: 0px;
}

.p-dialog-content {
  padding: 0px 12px 12px 12px;
  border: 5px solid;
  border-color: rgb(59, 73, 73);
  border-top-right-radius: 12px;
  border-top-left-radius: 12px;
  border-bottom-right-radius: 12px;
  border-bottom-left-radius: 12px;
}
/* .p-card-body{
  padding: 12px;
  border: 1px solid;
} */
</style>
