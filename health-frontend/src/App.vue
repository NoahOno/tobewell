<template>
  <el-config-provider>
    <router-view />
  </el-config-provider>
</template>

<script setup lang="ts">
</script>

<style>
@import './assets/styles/design-tokens.css';

body {
  margin: 0;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

#app {
  height: 100vh;
}
</style>
