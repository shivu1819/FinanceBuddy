import api from './api'

const asList = (data) => (Array.isArray(data) ? data : data?.content || [])

export async function getTransactions() {
  const response = await api.get('/transactions')
  return asList(response.data)
}

export async function getCategories() {
  const response = await api.get('/categories')
  return asList(response.data)
}

export async function createTransaction(payload) {
  const response = await api.post('/transactions', payload)
  return response.data
}

export async function updateTransaction(id, payload) {
  const response = await api.put(`/transactions/${id}`, payload)
  return response.data
}

export async function deleteTransaction(id) {
  await api.delete(`/transactions/${id}`)
}
