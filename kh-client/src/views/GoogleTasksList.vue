<template>
  <div>
    <ul>
      <li v-for="item in taskLists" v-bind:key="item.id">
        {{ item.title }}
      </li>
    </ul>
  </div>
</template>

<script>
import api from '../api';

export default {
  name: 'GoogleTasksList',
  data() {
    return {
      taskLists: [],
    };
  },
  methods: {
    listTaskLists() {
      const that = this;
      this.$getGapiClient()
        .then(() => {
          const options = { headers: { Authorization: `Bearer ${this.$auth.access_token}` } };
          api.getGTaskLists(options).then(
            (resp) => {
              that.taskLists = resp.data;
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
