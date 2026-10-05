import api from './api'

export async function scanReceipt(file) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await api.post('/receipts/scan', formData)
  return response.data
}
