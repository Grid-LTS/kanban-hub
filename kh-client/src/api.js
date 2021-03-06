import axios from 'axios';

const getGTaskProperties = () => axios.get('/api/google_tasks/properties');
const getGTaskLists = options => axios.get('/api/google_tasks/tasklists', options);
const getGTaskTasksForList = (taskListId, options) => axios.get(`/api/google_tasks/${taskListId}/tasks`, options);
const saveRecentGTaskCompleted = options => axios.post('/api/google_tasks/save', {}, options);
const saveAllGTaskCompleted = options => axios.post('/api/google_tasks/save?range=all', {}, options);

export default {
  getGTaskProperties,
  getGTaskLists,
  getGTaskTasksForList,
  saveRecentGTaskCompleted,
  saveAllGTaskCompleted,
};
