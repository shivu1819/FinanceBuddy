import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import Sidebar from '../../components/dashboard/Sidebar'
import Navbar from '../../components/dashboard/Navbar'
import CategoryAnalysis from '../../components/insights/CategoryAnalysis'
import FinancialHealthCard from '../../components/insights/FinancialHealthCard'
import InsightCard from '../../components/insights/InsightCard'
import MonthlyComparison from '../../components/insights/MonthlyComparison'
import RecommendationCard from '../../components/insights/RecommendationCard'
import SpendingPattern from '../../components/insights/SpendingPattern'
import TrendCard from '../../components/insights/TrendCard'
import useAuth from '../../hooks/useAuth'
import { getFinancialInsights } from '../../services/aiInsights.ts'

function InsightsSkeleton() { return <div className="animate-pulse space-y-6"><div className="h-28 rounded-2xl bg-slate-200" /><div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{Array.from({ length: 4 }, (_, index) => <div key={index} className="h-36 rounded-2xl bg-slate-200" />)}</div><div className="grid gap-6 xl:grid-cols-2"><div className="h-96 rounded-2xl bg-slate-200" /><div className="h-96 rounded-2xl bg-slate-200" /></div></div> }

function AIInsights() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [insights, setInsights] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const { user } = useAuth()

  useEffect(() => { let active = true; async function loadInsights() { try { const data = await getFinancialInsights(); if (active) setInsights(data) } catch (requestError) { if (active) { setError(requestError.response?.data?.message || 'Unable to load your financial insights. Please try again.'); toast.error('Unable to load financial insights.') } } finally { if (active) setLoading(false) } } loadInsights(); return () => { active = false } }, [])

  const metrics = insights?.metrics
  const recommendations = insights?.insights?.slice(0, 3) || []
  const hasData = insights && (insights.enoughData || insights.insights?.length)

  return <div className="flex min-h-screen bg-slate-50"><Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} /><div className="min-w-0 flex-1"><Navbar onMenuClick={() => setSidebarOpen(true)} user={user} /><main className="mx-auto max-w-7xl space-y-6 p-5 sm:p-8">{loading ? <InsightsSkeleton /> : error ? <section className="rounded-2xl border border-rose-200 bg-rose-50 px-6 py-20 text-center"><h2 className="text-lg font-bold text-rose-900">Insights unavailable</h2><p className="mx-auto mt-2 max-w-md text-sm text-rose-700">{error}</p></section> : hasData ? <><section><p className="text-sm font-medium text-violet-600">Personalized analysis</p><h2 className="mt-1 text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">AI financial insights</h2><p className="mt-2 text-sm text-slate-500">Rule-based analysis from your authenticated financial data.</p></section><section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4"><FinancialHealthCard score={metrics.healthScore} />{[{ label: 'Saving rate', value: `${metrics.savingRate}%`, tone: 'bg-emerald-100 text-emerald-700' }, { label: 'Expense ratio', value: `${metrics.expenseRatio}%`, tone: 'bg-rose-100 text-rose-700' }, { label: 'Budget utilization', value: `${metrics.budgetUtilization}%`, tone: 'bg-amber-100 text-amber-700' }].map((metric) => <article key={metric.label} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm shadow-slate-900/3"><span className={`inline-flex rounded-xl px-3 py-1 text-xs font-bold ${metric.tone}`}>{metric.label}</span><p className="mt-4 text-3xl font-bold tracking-tight text-slate-950">{metric.value}</p></article>)}</section><section className="grid gap-6 xl:grid-cols-[1.25fr_0.75fr]"><TrendCard data={insights.monthly} /><div className="space-y-6"><SpendingPattern categories={insights.categories} /><MonthlyComparison data={insights.monthly} /></div></section><section className="grid gap-6 xl:grid-cols-[1fr_0.9fr]"><CategoryAnalysis categories={insights.categories} /><div className="space-y-3"><h2 className="font-bold text-slate-950">Savings recommendations</h2>{recommendations.map((insight) => <RecommendationCard key={insight.title} recommendation={insight.recommendation} />)}</div></section><section><div className="mb-4"><h2 className="font-bold text-slate-950">Alerts and insights</h2><p className="mt-1 text-sm text-slate-500">Spending, budgets, savings, income, and goal progress.</p></div><div className="grid gap-4 xl:grid-cols-2">{insights.insights.map((insight) => <InsightCard key={`${insight.category}-${insight.title}`} insight={insight} />)}</div></section></> : <section className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-20 text-center"><h2 className="text-lg font-bold text-slate-800">No financial data yet</h2><p className="mx-auto mt-2 max-w-md text-sm text-slate-500">Add a transaction, budget, or savings goal to unlock personalized insights.</p></section>}</main></div></div>
}

export default AIInsights
