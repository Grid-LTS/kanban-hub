<template>
  <div>
    <div class="actions">
      <button @click="syncAllNewTaskUpdates">Sync all tasks with DB</button>
      <p>{{ apiMessage }}</p>
    </div>
    <div v-if="doneLoading">
      <GoogleTaskList v-for="list in taskLists" :key="list.id" :task-list="list"></GoogleTaskList>
    </div>
    <div v-else>Loading task lists...</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import GoogleTaskList from '../components/GoogleTaskList.vue'
import { useGoogleAuth } from '../composables/useGoogleAuth'

const { currentUser } = useGoogleAuth()

const taskLists = ref([])
const doneLoading = ref(false)
const apiMessage = ref('')

const listTaskLists = async () => {
  try {
    // Use the current user's auth token if available
    const headers = currentUser.value
      ? { Authorization: `Bearer ${currentUser.value.access_token}` }
      : {}

    const resp = await api.getGTaskLists({ headers })
    taskLists.value = resp.data
    doneLoading.value = true
  } catch (error) {
    console.error('Failed to load task lists:', error)
    doneLoading.value = true
  }
}

const syncAllNewTaskUpdates = async () => {
  try {
    const headers = currentUser.value
      ? { Authorization: `Bearer ${currentUser.value.access_token}` }
      : {}

    await api.saveRecentGTaskCompleted({ headers })
    apiMessage.value = 'Synced all recent task updates.'
  } catch (error) {
    console.error('Failed to sync tasks:', error)
    apiMessage.value = 'Error syncing tasks'
  }
}

onMounted(() => {
  listTaskLists()
})
</script>
