<template>
  <!--  html-->
  <div>
    <app-header v-bind:propsdata='str' v-on:renew='renewStr'></app-header>
  </div>

</template>

<script>

import AppHeader from './components/AppHeader.vue';

// JS
export default {
  // JS - 인스턴스 옵션
  data: function () {
    return {
      str: 'Header 123'
    }
  },
  components: {
    'app-header': AppHeader
  },
  methods: {
    renewStr: function () {
      this.str = 'hi'
    }
  }
}
</script>

<!--CSS-->
<style scoped>

</style>