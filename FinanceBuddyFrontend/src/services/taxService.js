import api from './api'

export async function calculateTax(payload) {
  const response = await api.post('/tax/calculate', payload)
  return response.data
}

export async function compareTaxRegimes(payload) {
  const response = await api.post('/tax/compare', payload)
  return response.data
}
