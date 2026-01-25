<template>
  <div>
    <span><b>{{ taskList.title }}</b></span>
    <br>
    <Task v-for="task in tasks" :key="task.id" :task="task"></Task>
    <br>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import Task from './Task.vue'
import { useGoogleAuth } from '../composables/useGoogleAuth'

const { currentUser } = useGoogleAuth()

const props = defineProps({
  taskList: {
    type: Object,
    required: true,
  },
})

const doneLoading = ref(false)
const tasks = ref([])

onMounted(async () => {
  try {
    let token = gapi.client.getToken();
    const headers = token.access_token
      ? { Authorization: `Bearer ${token.access_token}` }
      : {}

    const response = await api.getGTaskTasksForList(props.taskList.id, { headers })
    tasks.value = response.data
    doneLoading.value = true
  } catch (error) {
    console.error('Failed to load tasks:', error)
    doneLoading.value = true
  }
})
</script>
