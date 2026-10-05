import api from './api'

export async function getDashboardData() {
  const [summaryResponse, transactionsResponse, goalsResponse] = await Promise.all([
    api.get('/dashboard/summary'),
    api.get('/transactions'),
    api.get('/goals'),
  ])

  return {
    summary: summaryResponse.data,
    transactions: Array.isArray(transactionsResponse.data) ? transactionsResponse.data : transactionsResponse.data?.content || [],
    goals: Array.isArray(goalsResponse.data) ? goalsResponse.data : goalsResponse.data?.content || [],
  }
}
