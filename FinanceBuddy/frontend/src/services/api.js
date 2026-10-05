import axios from 'axios'

const api = axios.create({
  baseURL: 'https://financebuddy-i82n.onrender.com/api',
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('financebuddy_access_token')

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

export default api
