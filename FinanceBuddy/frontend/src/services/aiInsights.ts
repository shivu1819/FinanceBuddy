import api from './api'

export async function getFinancialInsights() {
  const { data } = await api.get('/ai-insights')
  const response = data || {}
  const insights = (response.insights || []).map((insight) => ({
    ...insight,
    description: insight.message,
  }))
  const monthly = (response.monthlyTrend || []).map((item) => ({ ...item, expense: item.expenses }))
  const categories = (response.topSpendingCategories || []).map((item) => ({ name: item.category, value: item.amount }))
  return {
    ...response,
    transactions: response.enoughData ? [{ id: 'summary' }] : [],
    goals: response.goalProgress || [],
    budget: response.budgetLimit > 0 ? { percentageUsed: response.budgetUtilization } : null,
    monthly,
    categories,
    insights,
    metrics: {
      healthScore: response.healthScore || 0,
      savingRate: response.savingsPercentage || 0,
      expenseRatio: response.totalIncome ? Math.round((response.totalExpenses / response.totalIncome) * 100) : 0,
      budgetUtilization: response.budgetUtilization || 0,
    },
  }
}
