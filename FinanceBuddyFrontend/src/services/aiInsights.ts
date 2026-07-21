import api from './api'

const number = (value) => Number(value) || 0
const monthKey = (date) => date?.slice(0, 7)

function monthlyTotals(transactions) {
  const months = new Map()
  transactions.forEach((transaction) => {
    const key = monthKey(transaction.transactionDate)
    if (!key) return
    const current = months.get(key) || { month: key, income: 0, expense: 0 }
    if (transaction.transactionType === 'INCOME') current.income += number(transaction.amount)
    if (transaction.transactionType === 'EXPENSE') current.expense += number(transaction.amount)
    months.set(key, current)
  })
  return [...months.values()].sort((a, b) => a.month.localeCompare(b.month))
}

function categoryTotals(transactions) {
  const totals = new Map()
  transactions.filter((transaction) => transaction.transactionType === 'EXPENSE').forEach((transaction) => {
    const name = transaction.category?.name || 'Uncategorized'
    totals.set(name, (totals.get(name) || 0) + number(transaction.amount))
  })
  return [...totals.entries()].map(([name, value]) => ({ name, value })).sort((a, b) => b.value - a.value)
}

function buildInsights({ summary, budget, goals, monthly, categories }) {
  const income = number(summary.totalIncome)
  const expenses = number(summary.totalExpense ?? summary.totalExpenses)
  const savingRate = income ? Math.round(((income - expenses) / income) * 100) : 0
  const insights = []
  const latest = monthly.at(-1)
  const previous = monthly.at(-2)
  const topCategory = categories[0]

  if (latest && previous && previous.expense) {
    const change = Math.round(((latest.expense - previous.expense) / previous.expense) * 100)
    if (change > 0) insights.push({ title: 'Expenses are trending upward', description: `Your expenses increased by ${change}% compared with the previous month.`, severity: change > 20 ? 'High' : 'Medium', recommendation: 'Review discretionary purchases and set a spending limit for the next month.' })
  }
  if (topCategory) insights.push({ title: 'Top spending category', description: `${topCategory.name} is currently your highest spending category.`, severity: 'Low', recommendation: `Review your ${topCategory.name} transactions for opportunities to reduce recurring costs.` })
  if (budget && number(budget.percentageUsed) >= 70) insights.push({ title: 'Budget needs attention', description: `You have used ${number(budget.percentageUsed)}% of your monthly budget.`, severity: number(budget.percentageUsed) > 90 ? 'High' : 'Medium', recommendation: 'Pause non-essential spending and monitor upcoming expenses closely.' })
  const closestGoal = goals.filter((goal) => goal.status !== 'COMPLETED').sort((a, b) => number(b.progressPercentage) - number(a.progressPercentage))[0]
  if (closestGoal) insights.push({ title: 'Goal progress is building', description: `${closestGoal.title} is ${number(closestGoal.progressPercentage)}% complete.`, severity: 'Low', recommendation: 'Keep contributing consistently to stay on track for this goal.' })
  if (savingRate >= 0) insights.push({ title: 'Saving rate overview', description: `Your current saving rate is ${savingRate}% based on recorded income and expenses.`, severity: savingRate < 10 ? 'Medium' : 'Low', recommendation: savingRate < 10 ? 'Aim to reduce one flexible expense category this month.' : 'Maintain your current saving habit and consider allocating surplus to a goal.' })
  return insights
}

export async function getFinancialInsights() {
  const [summaryResult, transactionsResult, goalsResult, budgetResult] = await Promise.allSettled([api.get('/dashboard/summary'), api.get('/transactions'), api.get('/goals'), api.get('/budgets/current')])
  const required = [summaryResult, transactionsResult, goalsResult]
  if (required.some((result) => result.status === 'rejected')) throw required.find((result) => result.status === 'rejected').reason
  const summary = summaryResult.value.data || {}
  const transactions = Array.isArray(transactionsResult.value.data) ? transactionsResult.value.data : transactionsResult.value.data?.content || []
  const goals = Array.isArray(goalsResult.value.data) ? goalsResult.value.data : goalsResult.value.data?.content || []
  const budget = budgetResult.status === 'fulfilled' ? budgetResult.value.data : null
  const monthly = monthlyTotals(transactions)
  const categories = categoryTotals(transactions)
  const income = number(summary.totalIncome)
  const expenses = number(summary.totalExpense ?? summary.totalExpenses)
  const savingRate = income ? Math.round(((income - expenses) / income) * 100) : 0
  const expenseRatio = income ? Math.round((expenses / income) * 100) : 0
  const budgetUtilization = number(budget?.percentageUsed)
  const healthScore = Math.min(100, Math.max(0, Math.round(100 - expenseRatio * 0.45 - Math.max(budgetUtilization - 70, 0) * 0.35 + Math.max(savingRate, 0) * 0.35)))
  return { summary, budget, goals, transactions, monthly, categories, insights: buildInsights({ summary, budget, goals, monthly, categories }), metrics: { healthScore, savingRate, expenseRatio, budgetUtilization } }
}
