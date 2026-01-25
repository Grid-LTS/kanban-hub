<template>
  <span>{{ task.title }}<br></span>
</template>

<script setup>
defineProps({
  task: {
    type: Object,
    required: true,
  },
})
</script>
