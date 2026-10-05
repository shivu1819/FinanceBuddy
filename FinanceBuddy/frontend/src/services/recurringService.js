import api from './api'
export const getRecurringTransactions = async () => (await api.get('/recurring-transactions')).data
export const createRecurringTransaction = async (payload) => (await api.post('/recurring-transactions', payload)).data
export const setRecurringActive = async (id, active) => (await api.patch(`/recurring-transactions/${id}/active?active=${active}`)).data
export const deleteRecurringTransaction = async (id) => api.delete(`/recurring-transactions/${id}`)
