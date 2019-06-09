<template>
  <div>
    Task
  </div>
</template>

<script>
export default {
  name: 'GoogleTasksList',
  methods: {
    listTaskLists() {
      this.$getGapiClient()
        .then((gapi) => {
          gapi.client.tasks.tasklists.list({
            maxResults: 10,
          }).then((response) => {
            const taskLists = response.result.items;
            taskLists.forEach((list) => {
              gapi.client.tasks.tasks.list({ tasklist: list.id });
            });
          });
        });
    },
  },
};

</script>
