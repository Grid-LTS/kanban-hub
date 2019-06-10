import axios from 'axios';

const getGTaskProperties = () => axios.get('/api/gtasks/properties');
const getGTaskLists = options => axios.get('/api/gtasks/tasklists', options);


export default {
  getGTaskProperties,
  getGTaskLists,
};
