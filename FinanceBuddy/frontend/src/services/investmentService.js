import api from './api'

export async function getInvestments() {
  const response = await api.get('/investments')
  return response.data
}

export async function getInvestment(id) {
  const response = await api.get(`/investments/${id}`)
  return response.data
}

export async function createInvestment(payload) {
  const response = await api.post('/investments', payload)
  return response.data
}

export async function updateInvestment(id, payload) {
  const response = await api.put(`/investments/${id}`, payload)
  return response.data
}

export async function deleteInvestment(id) {
  await api.delete(`/investments/${id}`)
}