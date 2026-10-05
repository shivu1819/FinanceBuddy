import { useEffect, useMemo, useState } from 'react'
import { ArrowTrendingDownIcon, ArrowTrendingUpIcon, BanknotesIcon, WalletIcon } from '@heroicons/react/24/outline'
import { Bar, BarChart, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import toast from 'react-hot-toast'
import Sidebar from '../../components/dashboard/Sidebar'
import Navbar from '../../components/dashboard/Navbar'
import SummaryCard from '../../components/dashboard/SummaryCard'
import ChartCard from '../../components/dashboard/ChartCard'
import ProgressCard from '../../components/dashboard/ProgressCard'
import QuickActions from '../../components/dashboard/QuickActions'
import RecentTransactions from '../../components/dashboard/RecentTransactions'
import useAuth from '../../hooks/useAuth'
import { getDashboardData } from '../../services/dashboardService'
import { formatCurrency, getBudgetProgress, getCurrentMonthTotals, getExpenseBreakdown, getGoalProgress, getMonthlyCashFlow, getPercentageChange, getRecentTransactions, getSummaryValue } from '../../utils/dashboardData'

const chartColors = ['#0f766e', '#34d399', '#818cf8', '#f59e0b', '#f43f5e', '#cbd5e1']

function DashboardSkeleton() {
  return <div className="animate-pulse space-y-7"><div><div className="h-4 w-32 rounded bg-slate-200" /><div className="mt-3 h-8 w-64 rounded bg-slate-200" /></div><div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{Array.from({ length: 4 }, (_, index) => <div key={index} className="h-40 rounded-2xl border border-slate-200 bg-white" />)}</div><div className="grid gap-6 xl:grid-cols-2">{Array.from({ length: 2 }, (_, index) => <div key={index} className="h-96 rounded-2xl border border-slate-200 bg-white" />)}</div><div className="grid gap-6 xl:grid-cols-2">{Array.from({ length: 2 }, (_, index) => <div key={index} className="h-80 rounded-2xl border border-slate-200 bg-white" />)}</div></div>
}

function EmptyChartState({ message }) {
  return <div className="grid h-72 place-items-center text-center text-sm text-slate-500">{message}</div>
}

function Dashboard() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [dashboard, setDashboard] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const { user } = useAuth()

  useEffect(() => {
    let isMounted = true

    async function loadDashboard() {
      try {
        const data = await getDashboardData()
        if (isMounted) setDashboard(data)
      } catch (error) {
        if (isMounted) toast.error(error.response?.data?.message || 'Unable to load your dashboard. Please try again.')
      } finally {
        if (isMounted) setIsLoading(false)
      }
    }

    loadDashboard()
    return () => { isMounted = false }
  }, [])

  const view = useMemo(() => {
    const transactions = dashboard?.transactions || []
    const summary = dashboard?.summary || {}
    const monthlyData = getMonthlyCashFlow(transactions)
    const expenseData = getExpenseBreakdown(transactions).map((item, index) => ({ ...item, color: chartColors[index % chartColors.length] }))
    const monthTotals = getCurrentMonthTotals(transactions)
    const income = getSummaryValue(summary, 'totalIncome', 'income', 'monthlyIncome')
    const expenses = getSummaryValue(summary, 'totalExpenses', 'totalExpense', 'expenses', 'monthlyExpenses')
    const balance = getSummaryValue(summary, 'totalBalance', 'balance', 'currentBalance')
    const savings = getSummaryValue(summary, 'totalSavings', 'savings', 'netSavings')

    return {
      transactions,
      monthlyData,
      expenseData,
      recentTransactions: getRecentTransactions(transactions),
      progress: [getBudgetProgress(summary), ...getGoalProgress(dashboard?.goals || [])].filter(Boolean),
      summaries: [
        { title: 'Total Balance', amount: formatCurrency(balance), change: getPercentageChange(balance, getSummaryValue(summary, 'previousBalance', 'lastMonthBalance')), icon: WalletIcon, accent: 'bg-emerald-100 text-emerald-700' },
        { title: 'Income', amount: formatCurrency(income), change: getPercentageChange(income || monthTotals.income, monthTotals.previousIncome), icon: ArrowTrendingUpIcon, accent: 'bg-sky-100 text-sky-700' },
        { title: 'Expenses', amount: formatCurrency(expenses), change: getPercentageChange(expenses || monthTotals.expense, monthTotals.previousExpense), icon: ArrowTrendingDownIcon, accent: 'bg-rose-100 text-rose-700', positive: false },
        { title: 'Savings', amount: formatCurrency(savings), change: getPercentageChange(savings, getSummaryValue(summary, 'previousSavings', 'lastMonthSavings')), icon: BanknotesIcon, accent: 'bg-violet-100 text-violet-700' },
      ],
      totalExpense: expenses || monthTotals.expense,
    }
  }, [dashboard])

  return <div className="flex min-h-screen bg-slate-50"><Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} /><div className="min-w-0 flex-1"><Navbar onMenuClick={() => setSidebarOpen(true)} user={user} /><main className="mx-auto max-w-7xl space-y-7 p-5 sm:p-8">{isLoading ? <DashboardSkeleton /> : <><section><p className="text-sm font-medium text-emerald-600">Your financial overview</p><h2 className="mt-1 text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">Your financial snapshot</h2></section><section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{view.summaries.map((summary) => <SummaryCard key={summary.title} {...summary} />)}</section><QuickActions /><section className="grid gap-6 xl:grid-cols-[1.45fr_1fr]"><ChartCard title="Income vs. expenses" subtitle="Your monthly cash flow">{view.monthlyData.length ? <><div className="h-72"><ResponsiveContainer width="100%" height="100%"><BarChart data={view.monthlyData} barGap={6}><XAxis dataKey="month" axisLine={false} tickLine={false} tick={{ fill: '#64748b', fontSize: 12 }} /><YAxis axisLine={false} tickLine={false} tick={{ fill: '#64748b', fontSize: 12 }} tickFormatter={(value) => formatCurrency(value)} /><Tooltip cursor={{ fill: '#f1f5f9' }} contentStyle={{ borderRadius: '12px', border: '1px solid #e2e8f0' }} formatter={(value) => formatCurrency(value)} /><Bar dataKey="income" name="Income" fill="#10b981" radius={[5, 5, 0, 0]} /><Bar dataKey="expense" name="Expenses" fill="#cbd5e1" radius={[5, 5, 0, 0]} /></BarChart></ResponsiveContainer></div><div className="mt-2 flex items-center justify-center gap-5 text-xs font-medium text-slate-500"><span className="flex items-center gap-1.5"><i className="size-2 rounded-full bg-emerald-500" /> Income</span><span className="flex items-center gap-1.5"><i className="size-2 rounded-full bg-slate-300" /> Expenses</span></div></> : <EmptyChartState message="No transactions yet. Your cash flow will appear here." />}</ChartCard><ChartCard title="Expense breakdown" subtitle="Where your money went">{view.expenseData.length ? <><div className="relative h-72"><ResponsiveContainer width="100%" height="100%"><PieChart><Pie data={view.expenseData} dataKey="value" nameKey="name" innerRadius={64} outerRadius={92} paddingAngle={4}>{view.expenseData.map((item) => <Cell key={item.name} fill={item.color} />)}</Pie><Tooltip contentStyle={{ borderRadius: '12px', border: '1px solid #e2e8f0' }} formatter={(value) => formatCurrency(value)} /></PieChart></ResponsiveContainer><div className="pointer-events-none absolute inset-0 grid place-items-center text-center"><div><p className="text-xl font-bold text-slate-950">{formatCurrency(view.totalExpense)}</p><p className="text-xs text-slate-500">spent</p></div></div></div><div className="grid grid-cols-2 gap-2">{view.expenseData.map((item) => <div key={item.name} className="flex items-center gap-2 text-xs text-slate-600"><i className="size-2 rounded-full" style={{ backgroundColor: item.color }} />{item.name} <span className="font-semibold text-slate-800">{formatCurrency(item.value)}</span></div>)}</div></> : <EmptyChartState message="No expense data is available yet." />}</ChartCard></section><section className="grid gap-6 xl:grid-cols-[0.9fr_1.1fr]"><section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm shadow-slate-900/3 sm:p-6"><h2 className="font-bold text-slate-950">Progress at a glance</h2><p className="mt-1 text-sm text-slate-500">Stay focused on what matters most.</p><div className="mt-5 space-y-3">{view.progress.length ? view.progress.map((progress) => <ProgressCard key={progress.id || progress.title} {...progress} spent={formatCurrency(progress.spent)} total={formatCurrency(progress.total)} />) : <p className="rounded-xl border border-dashed border-slate-200 px-4 py-10 text-center text-sm text-slate-500">No budget or goal progress to show yet.</p>}</div></section><RecentTransactions transactions={view.recentTransactions} /></section></>}</main></div></div>
}

export default Dashboard
