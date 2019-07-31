<template>
  <div>
  <span><b>{{ taskList.title }}</b></span>
  <br>
  <Task v-for="task in tasks" v-bind:key="task.id" v-bind:task="task"></Task>
    <br>
  </div>
</template>
<script>
import api from '../api';

import Task from './Task';

export default {
  name: 'GoogleTaskList',
  components: {
    Task,
  },
  props: ['taskList'],
  data() {
    return {
      doneLoading: false,
      tasks: [],
    };
  },
  created() {
    const that = this;
    const options = { headers: { Authorization: `Bearer ${this.$currentUser.access_token}` } };
    api.getGTaskTasksForList(this.taskList.id, options).then(
      (response) => {
        that.tasks = response.data;
        that.doneLoading = true;
      },
    );
  },
};
</script>
