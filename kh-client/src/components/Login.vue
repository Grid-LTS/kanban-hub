<template>
  <div>
    <!--Add buttons to initiate auth sequence and sign out-->
    <button ref="authorize_button" @click="handleAuthClick"
            v-bind:class="classAuthorize">Sign In</button>
    <button ref="signout_button" @click="handleSignoutClick"
            v-bind:class="classSignout">Sign Out</button>
  </div>
</template>

<script>

export default {
  name: 'Login',
  props: ['isSignedIn'],
  computed: {
    classAuthorize() {
      return {
        show: !this.isSignedIn,
        hide: this.isSignedIn,
      };
    },
    classSignout() {
      return {
        show: this.isSignedIn,
        hide: !this.isSignedIn,
      };
    },
  },
  methods: {
    handleAuthClick() {
      this.$login();
    },
    handleSignoutClick() {
      this.$logout();
    },
  },
};
</script>
