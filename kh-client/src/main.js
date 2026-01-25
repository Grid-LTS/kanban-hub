import { createApp } from 'vue'
import router from './router'
import App from './App.vue'
import api from './api'
import './styles/main.scss'

// Create app first
const app = createApp(App)

// Initialize Google Tasks configuration
const gTaskConfig = {
  discoveryDocs: ['https://www.googleapis.com/discovery/v1/apis/tasks/v1/rest'],
}

// Load properties and mount app
api.getGTaskProperties().then(
  (response) => {
    if (response.data) {
      Object.assign(gTaskConfig, response.data)
    }
    // Make gTaskConfig available globally
    app.config.globalProperties.$gTaskConfig = gTaskConfig
    
    app.use(router)
    app.mount('#app')
  },
  (error) => {
    console.error('Failed to load Google Tasks properties:', error)
    // Even on error, make config available and show login button
    app.config.globalProperties.$gTaskConfig = gTaskConfig
    app.config.globalProperties.$gTaskConfigError = error.message
    
    app.use(router)
    app.mount('#app')
  },
)





