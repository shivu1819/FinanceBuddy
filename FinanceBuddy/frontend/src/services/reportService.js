import api from './api'
export const getMonthlyReport = async (month) => (await api.get('/reports/monthly', { params: month ? { month } : {} })).data
export const exportMonthlyReport = async (month) => (await api.get('/reports/monthly/export', { params: month ? { month } : {}, responseType: 'blob' })).data
