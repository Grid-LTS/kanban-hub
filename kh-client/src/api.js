import axios from 'axios';

const getGTaskProperties = () => axios.get('/api/gtasks/properties');

export default {
  getGTaskProperties,
};
