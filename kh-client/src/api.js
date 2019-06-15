import axios from 'axios';

const getGTaskProperties = () => axios.get('/api/gtasks/properties');
const getGTaskLists = options => axios.get('/api/gtasks/tasklists', options);
const getGTaskTasksForList = (taskListId, options) => axios.get(`/api/gtasks/${taskListId}/tasks`, options);


export default {
  getGTaskProperties,
  getGTaskLists,
  getGTaskTasksForList,
};
