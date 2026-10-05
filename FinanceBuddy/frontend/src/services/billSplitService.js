import api from './api'
export const getBillSplits = async () => (await api.get('/bill-splits')).data
export const createBillSplit = async (payload) => (await api.post('/bill-splits', payload)).data
export const getBillSplit = async (id) => (await api.get(`/bill-splits/${id}`)).data
export const deleteBillSplit = async (id) => api.delete(`/bill-splits/${id}`)
