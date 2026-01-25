import { createRouter, createWebHistory } from 'vue-router'
import GoogleTasksList from './views/GoogleTaskLists.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'GoogleTasks',
      component: GoogleTasksList,
    },
  ],
})
