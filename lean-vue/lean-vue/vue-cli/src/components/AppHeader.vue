<template>
  <!--  html-->
  <header>
    <h1>{{ propsdata }}</h1>
    <button v-on:click="sendEvent">send</button>
  </header>

</template>

<script>
export default {
  props: ['propsdata'],
  methods: {
    sendEvent: function () {
      this.$emit('renew')
    }
  }
}
</script>

<!--CSS-->
<style scoped>

</style>