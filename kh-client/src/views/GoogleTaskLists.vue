<template>
  <div>
    <div class="actions">
      <button v-on:click="syncAllNewTaskUpdates">Sync all tasks with DB</button>
      <p>{{ apiMessage }}</p>
    </div>
    <div v-if="doneLoading">
      <GoogleTaskList v-for="list in taskLists" v-bind:key="list.id"
                      v-bind:task-list="list"></GoogleTaskList>
    </div>
  </div>
</template>

<script>
import api from '../api';
import GoogleTaskList from '../components/GoogleTaskList';

export default {
  name: 'GoogleTasksList',
  components: {
    GoogleTaskList,
  },
  data() {
    return {
      taskLists: [],
      doneLoading: false,
      apiMessage: '',
    };
  },
  methods: {
    listTaskLists() {
      this.$gapi.getGapiClient()
        .then(() => {
          const options = { headers: { Authorization: `Bearer ${this.$currentUser.access_token}` } };
          api.getGTaskLists(options).then(
            (resp) => {
              this.taskLists = resp.data;
              this.doneLoading = true;
            },
          );
        });
    },
    syncAllNewTaskUpdates() {
      const options = { headers: { Authorization: `Bearer ${this.$currentUser.access_token}` } };
      api.saveRecentGTaskCompleted(options).then(
        () => {
          this.apiMessage = 'Synced all recent task updates.';
        },
      );
    },
  },
  created() {
    this.listTaskLists();
  },
};

</script>
