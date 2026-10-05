import { PencilSquareIcon, TrashIcon } from '@heroicons/react/24/outline'
import { formatCurrency } from '../../utils/dashboardData'
import BudgetProgress from './BudgetProgress'

function BudgetTable({ budget, onEdit, onDelete }) {
  if (!budget) return null
  return <section className="overflow-x-auto rounded-2xl border border-slate-200 bg-white shadow-sm shadow-slate-900/3"><table className="w-full min-w-[760px] text-left"><thead className="border-b border-slate-200 bg-slate-50 text-xs font-semibold uppercase tracking-wide text-slate-500"><tr><th className="px-6 py-4">Month</th><th className="px-6 py-4">Budget</th><th className="px-6 py-4">Spent</th><th className="px-6 py-4">Remaining</th><th className="px-6 py-4">Progress</th><th className="px-6 py-4"><span className="sr-only">Actions</span></th></tr></thead><tbody><tr><td className="px-6 py-5 font-semibold text-slate-800">{budget.budgetMonth}</td><td className="px-6 py-5 text-sm text-slate-700">{formatCurrency(budget.monthlyLimit)}</td><td className="px-6 py-5 text-sm text-slate-700">{formatCurrency(budget.spent)}</td><td className="px-6 py-5 text-sm font-semibold text-slate-700">{formatCurrency(budget.remaining)}</td><td className="min-w-44 px-6 py-5"><BudgetProgress percentage={budget.percentageUsed} /></td><td className="px-6 py-5"><div className="flex justify-end gap-1"><button onClick={() => onEdit(budget)} className="rounded-lg p-2 text-slate-500 hover:bg-slate-100" aria-label="Edit budget"><PencilSquareIcon className="size-5" /></button><button onClick={() => onDelete(budget)} className="rounded-lg p-2 text-rose-500 hover:bg-rose-50" aria-label="Delete budget"><TrashIcon className="size-5" /></button></div></td></tr></tbody></table></section>
}

export default BudgetTable
