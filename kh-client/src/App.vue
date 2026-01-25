<template>
  <div id="app">
    <Login :isSignedIn="isSignedIn" @sign-in="handleAuthClick" @sign-out="handleSignoutClick"></Login>
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

const { isSignedIn, error, initializeGapiClient,gisLoaded,handleAuthClick, handleSignoutClick, initializeGoogleAuth, handleSignIn, handleSignOut,  } = useGoogleAuth()

onMounted(async () => {
  const apiScript = document.createElement("script");
  apiScript.defer = true
  apiScript.async = true
  apiScript.src = "https://apis.google.com/js/api.js"
  apiScript.onload = () => {
    gapi.load('client', initializeGapiClient);
  }
  document.body.appendChild(apiScript);


  const gsiScript = document.createElement("script");
  gsiScript.defer = true
  gsiScript.async = true
  gsiScript.src = "https://accounts.google.com/gsi/client"
  gsiScript.onload = () => {
    gisLoaded()
  }
  document.body.appendChild(gsiScript);
  //await initializeGoogleAuth()
})


</script>

