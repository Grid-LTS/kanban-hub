<template>
  <div>
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
    };
  },
  methods: {
    listTaskLists() {
      this.$getGapiClient()
        .then(() => {
          const options = { headers: { Authorization: `Bearer ${this.$auth.access_token}` } };
          api.getGTaskLists(options).then(
            (resp) => {
              this.taskLists = resp.data;
              this.doneLoading = true;
            },
          );
        });
    },
  },
  created() {
    this.listTaskLists();
  },
};

</script>
