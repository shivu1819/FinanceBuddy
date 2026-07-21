import { useEffect, useState } from 'react'
import { PlusIcon } from '@heroicons/react/24/outline'
import toast from 'react-hot-toast'
import Sidebar from '../../components/dashboard/Sidebar'
import Navbar from '../../components/dashboard/Navbar'
import BudgetCard from '../../components/budget/BudgetCard'
import BudgetModal from '../../components/budget/BudgetModal'
import BudgetSummary from '../../components/budget/BudgetSummary'
import BudgetTable from '../../components/budget/BudgetTable'
import DeleteDialog from '../../components/budget/DeleteDialog'
import useAuth from '../../hooks/useAuth'
import { createBudget, deleteBudget, getCurrentBudget, updateBudget } from '../../services/budgetService'

function BudgetSkeleton() {
  return <div className="animate-pulse space-y-6"><div className="h-28 rounded-2xl bg-slate-200" /><div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{Array.from({ length: 4 }, (_, index) => <div key={index} className="h-32 rounded-2xl bg-slate-200" />)}</div><div className="h-80 rounded-2xl bg-slate-200" /></div>
}

function Budget() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [budget, setBudget] = useState(null)
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const { user } = useAuth()

  useEffect(() => {
    let active = true
    async function loadBudget() {
      try {
        const data = await getCurrentBudget()
        if (active) setBudget(data)
      } catch (error) {
        if (active && error.response?.status !== 404) toast.error(error.response?.data?.message || 'Unable to load your budget. Please try again.')
      } finally {
        if (active) setLoading(false)
      }
    }
    loadBudget()
    return () => { active = false }
  }, [])

  const saveBudget = async (payload) => {
    setSubmitting(true)
    try {
      const saved = budget ? await updateBudget(budget.id, payload) : await createBudget(payload)
      setBudget(saved)
      setModalOpen(false)
      toast.success(budget ? 'Budget updated successfully.' : 'Budget created successfully.')
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to save the budget.')
    } finally {
      setSubmitting(false)
    }
  }

  const confirmDelete = async () => {
    if (!budget) return
    setSubmitting(true)
    try {
      await deleteBudget(budget.id)
      setBudget(null)
      setDeleteOpen(false)
      toast.success('Budget deleted successfully.')
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to delete the budget.')
    } finally {
      setSubmitting(false)
    }
  }

  return <div className="flex min-h-screen bg-slate-50"><Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} /><div className="min-w-0 flex-1"><Navbar onMenuClick={() => setSidebarOpen(true)} user={user} /><main className="mx-auto max-w-7xl space-y-6 p-5 sm:p-8">{loading ? <BudgetSkeleton /> : <><section className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end"><div><p className="text-sm font-medium text-emerald-600">Spending plan</p><h2 className="mt-1 text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">Budget</h2><p className="mt-2 text-sm text-slate-500">Set a monthly limit and track your spending as it happens.</p></div>{!budget && <button onClick={() => setModalOpen(true)} className="inline-flex items-center justify-center gap-2 rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-emerald-600/15 hover:bg-emerald-700"><PlusIcon className="size-5" /> Create budget</button>}</section>{budget ? <><BudgetSummary budget={budget} /><section className="grid gap-6 xl:grid-cols-[0.85fr_1.15fr]"><BudgetCard budget={budget} onEdit={() => setModalOpen(true)} onDelete={() => setDeleteOpen(true)} /><BudgetTable budget={budget} onEdit={() => setModalOpen(true)} onDelete={() => setDeleteOpen(true)} /></section></> : <section className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-20 text-center"><h3 className="text-lg font-bold text-slate-800">No budget for this month</h3><p className="mx-auto mt-2 max-w-sm text-sm text-slate-500">Create a monthly spending limit to see your progress and remaining balance here.</p><button onClick={() => setModalOpen(true)} className="mt-5 text-sm font-semibold text-emerald-600 hover:text-emerald-700">Create budget</button></section>}</>}</main></div>{modalOpen && <BudgetModal budget={budget} loading={submitting} onClose={() => setModalOpen(false)} onSubmit={saveBudget} />}<DeleteDialog budget={deleteOpen ? budget : null} loading={submitting} onCancel={() => setDeleteOpen(false)} onConfirm={confirmDelete} /></div>
}

export default Budget
