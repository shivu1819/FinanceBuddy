import { useEffect, useState } from 'react'
import { PlusIcon } from '@heroicons/react/24/outline'
import toast from 'react-hot-toast'
import Sidebar from '../../components/dashboard/Sidebar'
import Navbar from '../../components/dashboard/Navbar'
import GoalCard from '../../components/goals/GoalCard'
import GoalContributionModal from '../../components/goals/GoalContributionModal'
import GoalModal from '../../components/goals/GoalModal'
import GoalSummary from '../../components/goals/GoalSummary'
import GoalTable from '../../components/goals/GoalTable'
import DeleteDialog from '../../components/goals/DeleteDialog'
import useAuth from '../../hooks/useAuth'
import { contributeToGoal, createGoal, deleteGoal, getGoals, updateGoal } from '../../services/goalService'

function GoalsSkeleton() { return <div className="animate-pulse space-y-6"><div className="h-28 rounded-2xl bg-slate-200" /><div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{Array.from({ length: 4 }, (_, index) => <div key={index} className="h-32 rounded-2xl bg-slate-200" />)}</div><div className="grid gap-5 md:grid-cols-2">{Array.from({ length: 2 }, (_, index) => <div key={index} className="h-72 rounded-2xl bg-slate-200" />)}</div></div> }

function Goals() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [goals, setGoals] = useState([])
  const [loading, setLoading] = useState(true)
  const [goalModal, setGoalModal] = useState(undefined)
  const [contributionGoal, setContributionGoal] = useState(null)
  const [deleteGoalTarget, setDeleteGoalTarget] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const { user } = useAuth()

  useEffect(() => {
    let active = true
    async function loadGoals() {
      try { const data = await getGoals(); if (active) setGoals(data) } catch (error) { if (active) toast.error(error.response?.data?.message || 'Unable to load goals. Please try again.') } finally { if (active) setLoading(false) }
    }
    loadGoals()
    return () => { active = false }
  }, [])

  const saveGoal = async (payload) => {
    setSubmitting(true)
    try {
      if (goalModal?.id) { const updated = await updateGoal(goalModal.id, payload); setGoals((items) => items.map((item) => item.id === updated.id ? updated : item)); toast.success('Goal updated successfully.') } else { const created = await createGoal(payload); setGoals((items) => [created, ...items]); toast.success('Goal created successfully.') }
      setGoalModal(undefined)
    } catch (error) { toast.error(error.response?.data?.message || 'Unable to save the goal.') } finally { setSubmitting(false) }
  }

  const addContribution = async (amount) => {
    if (!contributionGoal) return
    setSubmitting(true)
    try {
      const updated = await contributeToGoal(contributionGoal.id, amount)
      setGoals((items) => items.map((item) => item.id === updated.id ? updated : item))
      setContributionGoal(null)
      toast.success(updated.status === 'COMPLETED' ? 'Goal completed—amazing work!' : 'Contribution added successfully.')
    } catch (error) { toast.error(error.response?.data?.message || 'Unable to add the contribution.') } finally { setSubmitting(false) }
  }

  const confirmDelete = async () => {
    if (!deleteGoalTarget) return
    setSubmitting(true)
    try { await deleteGoal(deleteGoalTarget.id); setGoals((items) => items.filter((item) => item.id !== deleteGoalTarget.id)); setDeleteGoalTarget(null); toast.success('Goal deleted successfully.') } catch (error) { toast.error(error.response?.data?.message || 'Unable to delete the goal.') } finally { setSubmitting(false) }
  }

  return <div className="flex min-h-screen bg-slate-50"><Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} /><div className="min-w-0 flex-1"><Navbar onMenuClick={() => setSidebarOpen(true)} user={user} /><main className="mx-auto max-w-7xl space-y-6 p-5 sm:p-8">{loading ? <GoalsSkeleton /> : <><section className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end"><div><p className="text-sm font-medium text-violet-600">Future planning</p><h2 className="mt-1 text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">Savings goals</h2><p className="mt-2 text-sm text-slate-500">Track every milestone and make steady progress toward what matters.</p></div><button onClick={() => setGoalModal(null)} className="inline-flex items-center justify-center gap-2 rounded-xl bg-violet-600 px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-violet-600/15 hover:bg-violet-700"><PlusIcon className="size-5" /> Create goal</button></section>{goals.length ? <><GoalSummary goals={goals} /><GoalTable goals={goals} onContribute={setContributionGoal} onEdit={setGoalModal} onDelete={setDeleteGoalTarget} /><section className="grid gap-5 md:grid-cols-2 xl:hidden">{goals.map((goal) => <GoalCard key={goal.id} goal={goal} onContribute={setContributionGoal} onEdit={setGoalModal} onDelete={setDeleteGoalTarget} />)}</section></> : <section className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-20 text-center"><h3 className="text-lg font-bold text-slate-800">No savings goals yet</h3><p className="mx-auto mt-2 max-w-sm text-sm text-slate-500">Create a goal to make your next financial milestone feel achievable.</p><button onClick={() => setGoalModal(null)} className="mt-5 text-sm font-semibold text-violet-600 hover:text-violet-700">Create goal</button></section>}</>}</main></div>{goalModal !== undefined && <GoalModal goal={goalModal} loading={submitting} onClose={() => setGoalModal(undefined)} onSubmit={saveGoal} />}{contributionGoal && <GoalContributionModal goal={contributionGoal} loading={submitting} onClose={() => setContributionGoal(null)} onSubmit={addContribution} />}<DeleteDialog goal={deleteGoalTarget} loading={submitting} onCancel={() => setDeleteGoalTarget(null)} onConfirm={confirmDelete} /></div>
}
export default Goals
