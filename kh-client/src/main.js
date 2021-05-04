// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue';
import VueGapi from 'vue-gapi';
import App from './App';
import router from './router';
import './styles/main.scss';
import api from './api';

Vue.config.productionTip = false;

const gTaskConfig = {
  discoveryDocs: ['https://www.googleapis.com/discovery/v1/apis/tasks/v1/rest'],
};

api.getGTaskProperties().then(
  (response) => {
    if (response.data) {
      Object.assign(gTaskConfig, response.data);
      Vue.use(VueGapi, gTaskConfig);
      /* eslint-disable no-new */
      new Vue({
        el: '#app',
        router,
        components: { App },
        template: '<App/>',
      });
    }
  },
);
