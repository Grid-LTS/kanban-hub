<template>
  <div id="app">
    <Login :isSignedIn="isSignedIn" @sign-in="handleSignIn" @sign-out="handleSignOut"></Login>
    <div v-if="isSignedIn" class="views">
      <RouterView />
    </div>
    <pre v-if="error" id="content" style="white-space: pre-wrap;">{{ error }}</pre>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import Login from './components/Login.vue'
import { useGoogleAuth } from './composables/useGoogleAuth'

const { isSignedIn, error, initializeGoogleAuth, handleSignIn, handleSignOut } = useGoogleAuth()

onMounted(async () => {
  await initializeGoogleAuth()
})
</script>
