<template>
  <span>{{ task.title }}<br></span>
</template>
<script>
export default {
  name: 'Task',
  props: ['task'],
};
</script>
