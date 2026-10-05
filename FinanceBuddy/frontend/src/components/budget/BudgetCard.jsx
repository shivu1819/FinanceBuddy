import { PencilSquareIcon, TrashIcon } from '@heroicons/react/24/outline'
import { formatCurrency } from '../../utils/dashboardData'
import BudgetProgress from './BudgetProgress'

function BudgetCard({ budget, onEdit, onDelete }) {
  const month = new Intl.DateTimeFormat('en-IN', { month: 'long', year: 'numeric' }).format(new Date(`${budget.budgetMonth}-01T00:00:00`))
  return <article className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm shadow-slate-900/3"><div className="flex items-start justify-between gap-4"><div><p className="text-sm font-medium text-emerald-600">{month}</p><h3 className="mt-1 text-xl font-bold text-slate-950">Monthly budget</h3></div><div className="flex gap-1"><button onClick={() => onEdit(budget)} className="rounded-lg p-2 text-slate-500 hover:bg-slate-100" aria-label="Edit budget"><PencilSquareIcon className="size-5" /></button><button onClick={() => onDelete(budget)} className="rounded-lg p-2 text-rose-500 hover:bg-rose-50" aria-label="Delete budget"><TrashIcon className="size-5" /></button></div></div><div className="mt-6 grid grid-cols-2 gap-4 text-sm"><div><p className="text-slate-500">Total budget</p><p className="mt-1 font-bold text-slate-900">{formatCurrency(budget.monthlyLimit)}</p></div><div><p className="text-slate-500">Amount spent</p><p className="mt-1 font-bold text-slate-900">{formatCurrency(budget.spent)}</p></div></div><div className="mt-6"><BudgetProgress percentage={budget.percentageUsed} /></div></article>
}

export default BudgetCard
