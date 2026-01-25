import { ref, onBeforeMount, getCurrentInstance } from 'vue'

let gapiClient = null

export const useGoogleAuth = () => {
  const isSignedIn = ref(false)
  const currentUser = ref(null)
  const error = ref('')
  const instance = getCurrentInstance()
  const gTaskConfig = instance?.appContext.config.globalProperties.$gTaskConfig

  const initializeGoogleAuth = async () => {
    return new Promise((resolve) => {
      // Check if clientId is available
      if (!gTaskConfig?.clientId) {
        error.value = 'Google Client ID not configured. Please check your server configuration.'
        resolve(null)
        return
      }

      // Load the Google Identity Services script
      const script = document.createElement('script')
      script.src = 'https://accounts.google.com/gsi/client'
      script.async = true
      script.defer = true

      script.onload = () => {
        if (window.google && window.google.accounts) {
          try {
            // Initialize Google Sign-In
            window.google.accounts.id.initialize({
              client_id: gTaskConfig.clientId,
              callback: handleCredentialResponse,
            })

            // Render the sign-in button
            const signInButton = document.getElementById('google-sign-in-button')
            if (signInButton) {
              window.google.accounts.id.renderButton(
                signInButton,
                { theme: 'outline', size: 'large' },
              )
            }

            resolve(window.google)
          } catch (err) {
            error.value = `Failed to initialize Google Auth: ${err.message}`
            resolve(null)
          }
        }
      }

      script.onerror = () => {
        error.value = 'Failed to load Google Identity Services'
        resolve(null)
      }

      document.head.appendChild(script)
    })
  }

  const handleCredentialResponse = (response) => {
    if (response.credential) {
      // Decode the JWT token to get user info
      try {
        const base64Url = response.credential.split('.')[1]
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
        const jsonPayload = decodeURIComponent(
          atob(base64)
            .split('')
            .map((c) => `%${(`00${c.charCodeAt(0).toString(16)}`).slice(-2)}`)
            .join(''),
        )
        currentUser.value = JSON.parse(jsonPayload)
        isSignedIn.value = true
      } catch (err) {
        error.value = `Failed to decode Google token: ${err.message}`
      }
    }
  }

  const handleSignIn = () => {
    if (window.google && window.google.accounts) {
      window.google.accounts.id.prompt((notification) => {
        if (notification.isNotDisplayed()) {
          // The notification is not displayed anymore
          error.value = 'Google Sign-In prompt could not be displayed'
        }
      })
    }
  }

  const handleSignOut = () => {
    if (window.google && window.google.accounts) {
      window.google.accounts.id.disableAutoSelect()
    }
    isSignedIn.value = false
    currentUser.value = null
  }

  onBeforeMount(async () => {
    gapiClient = await initializeGoogleAuth()
  })

  return {
    isSignedIn,
    currentUser,
    error,
    initializeGoogleAuth,
    handleSignIn,
    handleSignOut,
  }
}
