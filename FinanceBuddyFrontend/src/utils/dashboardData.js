const currencyFormatter = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 })
const monthFormatter = new Intl.DateTimeFormat('en-IN', { month: 'short', year: 'numeric' })

export const formatCurrency = (value) => currencyFormatter.format(Number(value) || 0)

const toAmount = (value) => Number(value) || 0

export function getMonthlyCashFlow(transactions) {
  const months = new Map()

  transactions.forEach((transaction) => {
    if (!transaction.transactionDate) return
    const date = new Date(`${transaction.transactionDate}T00:00:00`)
    const key = `${date.getFullYear()}-${date.getMonth()}`
    const month = months.get(key) || { month: monthFormatter.format(date), date, income: 0, expense: 0 }
    const type = transaction.transactionType?.toUpperCase()

    if (type === 'INCOME') month.income += toAmount(transaction.amount)
    if (type === 'EXPENSE') month.expense += toAmount(transaction.amount)
    months.set(key, month)
  })

  return [...months.values()].sort((a, b) => a.date - b.date).map(({ month, income, expense }) => ({ month, income, expense }))
}

export function getExpenseBreakdown(transactions) {
  const expenses = new Map()

  transactions.filter((transaction) => transaction.transactionType?.toUpperCase() === 'EXPENSE').forEach((transaction) => {
    const category = transaction.category?.name || 'Uncategorized'
    expenses.set(category, (expenses.get(category) || 0) + toAmount(transaction.amount))
  })

  return [...expenses.entries()].map(([name, value]) => ({ name, value }))
}

export function getRecentTransactions(transactions) {
  return [...transactions].sort((a, b) => new Date(b.transactionDate) - new Date(a.transactionDate)).slice(0, 5)
}

export function formatTransactionDate(date) {
  if (!date) return 'Date unavailable'
  return new Intl.DateTimeFormat('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }).format(new Date(`${date}T00:00:00`))
}

export function getSummaryValue(summary, ...keys) {
  for (const key of keys) {
    const value = key.split('.').reduce((current, part) => current?.[part], summary)
    if (value !== undefined && value !== null) return toAmount(value)
  }

  return 0
}

export function getPercentageChange(current, previous) {
  if (!previous) return '—'
  const change = ((current - previous) / Math.abs(previous)) * 100
  return `${change > 0 ? '+' : ''}${change.toFixed(1)}%`
}

export function getCurrentMonthTotals(transactions) {
  const now = new Date()
  const currentMonth = now.getMonth()
  const currentYear = now.getFullYear()
  const previousMonthDate = new Date(currentYear, currentMonth - 1, 1)
  const totals = { income: 0, expense: 0, previousIncome: 0, previousExpense: 0 }

  transactions.forEach((transaction) => {
    if (!transaction.transactionDate) return
    const date = new Date(`${transaction.transactionDate}T00:00:00`)
    const amount = toAmount(transaction.amount)
    const type = transaction.transactionType?.toUpperCase()
    const isCurrent = date.getFullYear() === currentYear && date.getMonth() === currentMonth
    const isPrevious = date.getFullYear() === previousMonthDate.getFullYear() && date.getMonth() === previousMonthDate.getMonth()

    if (type === 'INCOME') {
      if (isCurrent) totals.income += amount
      if (isPrevious) totals.previousIncome += amount
    }

    if (type === 'EXPENSE') {
      if (isCurrent) totals.expense += amount
      if (isPrevious) totals.previousExpense += amount
    }
  })

  return totals
}

export function getBudgetProgress(summary) {
  const budget = summary?.budgetProgress || summary?.budget || {}
  const spent = getSummaryValue(budget, 'spent', 'used', 'spentAmount', 'currentSpending') || getSummaryValue(summary, 'spentBudget')
  const total = getSummaryValue(budget, 'total', 'limit', 'budgetAmount', 'amount') || getSummaryValue(summary, 'monthlyBudget')
  const percentage = getSummaryValue(budget, 'percentageUsed', 'usagePercentage') || getSummaryValue(summary, 'budgetUsagePercentage')

  if (!total) return null

  return {
    title: budget.name || 'Monthly budget',
    description: budget.period || 'Current spending limit',
    spent,
    total,
    percent: percentage || Math.round((spent / total) * 100),
    tone: 'emerald',
  }
}

export function getGoalProgress(goals) {
  return goals.map((goal, index) => {
    const saved = getSummaryValue(goal, 'currentAmount', 'savedAmount', 'amountSaved', 'saved')
    const target = getSummaryValue(goal, 'targetAmount', 'target', 'goalAmount', 'amount')

    return {
      id: goal.id || goal.goalId || `${goal.name || goal.title}-${index}`,
      title: goal.name || goal.title || 'Savings goal',
      description: goal.targetDate ? `Target: ${formatTransactionDate(goal.targetDate)}` : 'Savings goal',
      spent: saved,
      total: target,
      percent: target ? Math.round((saved / target) * 100) : 0,
      tone: index % 2 ? 'amber' : 'violet',
    }
  })
}
