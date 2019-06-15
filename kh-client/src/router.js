import Vue from 'vue';
import Router from 'vue-router';
import GoogleTasksList from './views/GoogleTaskLists';

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
