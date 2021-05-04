<template>
  <div id="app">
    <Login v-bind:isSignedIn="isSignedIn"></Login>
    <div v-if="isSignedIn" class="views">
      <router-view/>
    </div>
    <pre v-if="error" id="content" style="white-space: pre-wrap;">{{error}}</pre>
  </div>
</template>

<script>
import Vue from 'vue';
import Login from './components/Login';


export default {
  name: 'App',
  data() {
    return {
      isSignedIn: false,
      error: '',
    };
  },
  components: { Login },
  methods: {
    updateIsSignedIn() {
      this.isSignedIn = this.$gapi.isAuthenticated();
    },
  },
  created() {
    this.$gapi.getGapiClient()
      .then((gapi) => {
        Object.defineProperty(Vue.prototype, '$currentUser', {
          get() { return gapi.auth2.getAuthInstance().currentUser.get().getAuthResponse(true); },
        });
        // Listen for sign-in state changes.
        gapi.auth2.getAuthInstance().isSignedIn.listen(this.updateIsSignedIn);
        // Handle the initial sign-in state.
        this.updateIsSignedIn();
      }, (error) => {
        this.error = JSON.stringify(error, null, 2);
      });
  },
};
</script>
