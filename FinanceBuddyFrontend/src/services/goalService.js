import api from './api'

export async function getGoals() {
  const response = await api.get('/goals')
  return Array.isArray(response.data) ? response.data : response.data?.content || []
}

export async function createGoal(payload) { const response = await api.post('/goals', payload); return response.data }
export async function updateGoal(id, payload) { const response = await api.put(`/goals/${id}`, payload); return response.data }
export async function contributeToGoal(id, amount) { const response = await api.patch(`/goals/${id}/add-money`, { amount }); return response.data }
export async function deleteGoal(id) { await api.delete(`/goals/${id}`) }
