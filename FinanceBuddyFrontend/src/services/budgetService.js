import api from './api'

export async function getCurrentBudget() {
  const response = await api.get('/budgets/current')
  return response.data
}

export async function createBudget(payload) {
  const response = await api.post('/budgets', payload)
  return response.data
}

export async function updateBudget(id, payload) {
  const response = await api.put(`/budgets/${id}`, payload)
  return response.data
}

export async function deleteBudget(id) {
  await api.delete(`/budgets/${id}`)
}
