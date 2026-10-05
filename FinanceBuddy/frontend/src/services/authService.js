import api from './api'

export const loginRequest = (credentials) => api.post('/auth/login', credentials)

export const registerRequest = (details) => api.post('/auth/register', details)
