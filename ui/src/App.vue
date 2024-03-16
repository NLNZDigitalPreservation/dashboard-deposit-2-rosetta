<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import HelloWorld from './components/HelloWorld.vue'
import MainNavBar from './components/MainNavBar.vue'
</script>

<template>

  
  <header>
    
    <MainNavBar></MainNavBar>
  </header>

  <RouterView />
</template>

<style scoped>


</style>
