<template>
  <el-config-provider>
    <router-view v-slot="{ Component }">
      <transition name="page" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </el-config-provider>
</template>

<script setup lang="ts">
</script>

<style>
@import './assets/styles/design-tokens.css';

body {
  margin: 0;
  font-family: var(--font-family);
}

#app {
  height: 100vh;
}

.page-enter-active,
.page-leave-active {
  transition: opacity 260ms var(--ease-out), transform 260ms var(--ease-out);
}
.page-enter-from,
.page-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
