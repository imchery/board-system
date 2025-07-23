<template>
  <div id="app">
    <!-- Vue Router가 페이지를 여기에 렌더링 -->
    <router-view />
  </div>
</template>

<script setup lang="ts">
// Vue 3 Composition API + TypeScript
// setup은 Vue 3에서 추천하는 문법
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Element Plus와 충돌 방지 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>
