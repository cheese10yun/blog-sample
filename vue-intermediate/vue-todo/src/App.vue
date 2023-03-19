<template>
  <div id="app">

    <TodoFooter></TodoFooter>
    <TodoHeader></TodoHeader>
    <TodoList></TodoList>
    <TodoInput></TodoInput>
  </div>
</template>

<script>
import TodoFooter from './components/TodoFooter.vue'
import TodoHeader from './components/TodoHeader.vue'
import TodoList from './components/TodoList.vue'
import TodoInput from './components/TodoInput.vue'

export default {
  components: {
    'TodoFooter': TodoFooter,
    'TodoHeader': TodoHeader,
    'TodoList': TodoList,
    'TodoInput': TodoInput
  }

}
</script>

<style>

</style>
