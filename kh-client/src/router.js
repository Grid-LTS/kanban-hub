import Vue from 'vue';
import Router from 'vue-router';
import GoogleTasksList from './views/GoogleTasksList';

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Google Tasks',
      component: GoogleTasksList,
    },
  ],
});
