import api from './api'

const asList = (data) => (Array.isArray(data) ? data : data?.content || [])

export async function getCategories() {
  const response = await api.get('/categories')
  return asList(response.data)
}

export async function createCategory(payload) {
  const response = await api.post('/categories', {
    ...payload,
    name: payload.name?.trim(),
    type: payload.type?.toUpperCase(),
  }, { timeout: 15000 })
  return response.data
}

export async function updateCategory(id, payload) {
  const response = await api.put(`/categories/${id}`, payload)
  return response.data
}

export async function deleteCategory(id) {
  await api.delete(`/categories/${id}`)
}
