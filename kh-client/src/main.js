import { createApp } from 'vue'
import router from './router'
import App from './App.vue'
import api from './api'
import './styles/main.scss'
// Initialize Google Tasks configuration

const gTaskConfig = {
  discoveryDocs: ['https://www.googleapis.com/discovery/v1/apis/tasks/v1/rest'],
}
api.getGTaskProperties().then(
  (response) => {
    if (response.data) {
      Object.assign(gTaskConfig, response.data)
      // Make gTaskConfig available globally if needed
      app.config.globalProperties.$gTaskConfig = gTaskConfig
    }
    app.use(router)
    app.mount('#app')
  },
  (error) => {
    console.error('Failed to load Google Tasks properties:', error)
    app.use(router)
    app.mount('#app')
  },
)


const app = createApp(App)





