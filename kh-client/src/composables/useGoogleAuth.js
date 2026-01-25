import { ref, onBeforeMount, getCurrentInstance } from 'vue'

// Shared state across all components (singleton pattern)
const isSignedIn = ref(false)
const currentUser = ref(null)
const error = ref('')

// https://developers.google.com/workspace/tasks/quickstart/js?hl=de

export const useGoogleAuth = () => {
  const instance = getCurrentInstance()
  const gTaskConfig = instance?.appContext.config.globalProperties.$gTaskConfig

  const CLIENT_ID = gTaskConfig.clientId;
  const API_KEY = gTaskConfig.apiKey;

  // Discovery doc URL for APIs used by the quickstart
  const DISCOVERY_DOC = 'https://www.googleapis.com/discovery/v1/apis/tasks/v1/rest';

  // Authorization scopes required by the API; multiple scopes can be
  // included, separated by spaces.
  const SCOPES = 'https://www.googleapis.com/auth/tasks.readonly';

  let tokenClient;
  let gapiInited = false;
  let gisInited = false;


  /**
   * Callback after api.js is loaded.
   */
  const gapiLoaded = () => {

  }

  /**
   * Callback after the API client is loaded. Loads the
   * discovery doc to initialize the API.
   */
  async function initializeGapiClient() {
    await gapi.client.init({
      apiKey: API_KEY,
      discoveryDocs: [DISCOVERY_DOC],
    });
    gapiInited = true;
  }

  /**
   * Callback after Google Identity Services are loaded.
   */
  function gisLoaded() {
    tokenClient = google.accounts.oauth2.initTokenClient({
      client_id: CLIENT_ID,
      scope: SCOPES,
      callback: '', // defined later
    });
    gisInited = true;
  }

  /**
   *  Sign in the user upon button click.
   */
  const handleAuthClick = () => {
    tokenClient.callback = async (resp) => {
      if (resp.error !== undefined) {
        throw (resp);
      }
      document.getElementById('google-sign-in-button').innerText = 'Refresh';
      isSignedIn.value = true
      // Todo emit event that login is successfull
    };

    if (gapi.client.getToken() === null) {
      // Prompt the user to select a Google Account and ask for consent to share their data
      // when establishing a new session.
      tokenClient.requestAccessToken({prompt: 'consent'});
      currentUser.accessToken = gapi.client.getToken()
    } else {
      // Skip display of account chooser and consent dialog for an existing session.
      tokenClient.requestAccessToken({prompt: ''});
    }

  }

  /**
   *  Sign out the user upon button click.
   */
  const handleSignoutClick = () => {
    const token = gapi.client.getToken();
    if (token !== null) {
      google.accounts.oauth2.revoke(token.access_token);
      gapi.client.setToken('');
      document.getElementById('google-sign-in-button').innerText = 'Authorize';
    }
    isSignedIn.value = false
  }


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
        const tokencontent = JSON.parse(jsonPayload)
        currentUser.accessToken = response.credential
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

  /** Only initialize once
  if (!gapiClient) {
    onBeforeMount(async () => {
      gapiClient = await initializeGoogleAuth()
    })
  }**/

  return {
    isSignedIn,
    currentUser,
    error,
    gapiLoaded,
    initializeGapiClient,
    gisLoaded,
    handleAuthClick,
    handleSignoutClick,
    initializeGoogleAuth,
    handleSignIn,
    handleSignOut,
  }
}
