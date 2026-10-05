import api from './api'

export async function getProfile() { const response = await api.get('/profile'); return response.data }
export async function updateProfile(payload) { const response = await api.put('/profile', payload); return response.data }
