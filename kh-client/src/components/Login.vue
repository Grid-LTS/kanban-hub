<template>
  <div>
    <!--Add buttons to initiate auth sequence and sign out-->
    <button v-if="!isSignedIn"  id="google-sign-in-button" @click="$emit('sign-in')">Authorize</button>
    <button v-if="isSignedIn" @click="$emit('sign-out')" class="sign-out-btn">Sign Out</button>
  </div>
</template>

<script setup>
defineProps({
  isSignedIn: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['sign-in', 'sign-out'])
</script>

<style scoped>
.sign-out-btn {
  padding: 8px 16px;
  background-color: #d32f2f;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.sign-out-btn:hover {
  background-color: #b71c1c;
}
</style>
