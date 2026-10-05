import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { XMarkIcon } from '@heroicons/react/24/outline'
import { getCategories } from '../../services/categoryService'

const currentMonth = () => new Date().toISOString().slice(0, 7)

function BudgetModal({ budget, categories: initialCategories = [], loading, onClose, onSubmit }) {
  const [availableCategories, setAvailableCategories] = useState(initialCategories)
  const categories = availableCategories
  useEffect(() => { if (!initialCategories.length) getCategories().then((items) => setAvailableCategories(items.filter((category) => category.type === 'EXPENSE'))).catch(() => {}) }, [initialCategories])
  const editing = Boolean(budget)
  const { register, handleSubmit, formState: { errors } } = useForm({
    defaultValues: budget
      ? { monthlyLimit: budget.monthlyLimit, budgetMonth: budget.budgetMonth, categoryId: String(budget.categoryId || '') }
      : { monthlyLimit: '', budgetMonth: currentMonth(), categoryId: '' },
  })
  const submit = (values) => onSubmit({ monthlyLimit: Number(values.monthlyLimit), budgetMonth: values.budgetMonth, categoryId: Number(values.categoryId) })

  return <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-950/45 p-4 sm:grid sm:place-items-center" role="dialog" aria-modal="true" aria-labelledby="budget-modal-title"><section className="my-6 w-full max-w-lg rounded-2xl bg-white p-5 shadow-2xl sm:my-0 sm:p-6"><div className="flex items-start justify-between gap-4"><div><h2 id="budget-modal-title" className="text-xl font-bold text-slate-950">{editing ? 'Edit budget' : 'Create budget'}</h2><p className="mt-1 text-sm text-slate-500">Set a category spending limit.</p></div><button onClick={onClose} disabled={loading} className="rounded-lg p-2 text-slate-500 hover:bg-slate-100" aria-label="Close budget modal"><XMarkIcon className="size-5" /></button></div><form onSubmit={handleSubmit(submit)} className="mt-6 grid gap-4"><label><span className="mb-1.5 block text-sm font-semibold text-slate-700">Category</span><select {...register('categoryId', { required: 'Category is required' })} className="w-full rounded-xl border border-slate-200 bg-white px-3 py-3 text-sm"><option value="">Select expense category</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select>{errors.categoryId && <span className="mt-1 block text-xs font-medium text-rose-600">{errors.categoryId.message}</span>}</label><label><span className="mb-1.5 block text-sm font-semibold text-slate-700">Monthly budget</span><input type="number" step="0.01" min="0.01" {...register('monthlyLimit', { required: 'Monthly budget is required', valueAsNumber: true, min: { value: 0.01, message: 'Budget must be greater than zero' } })} placeholder="0.00" className="w-full rounded-xl border border-slate-200 px-3 py-3 text-sm" />{errors.monthlyLimit && <span className="mt-1 block text-xs font-medium text-rose-600">{errors.monthlyLimit.message}</span>}</label><label><span className="mb-1.5 block text-sm font-semibold text-slate-700">Budget month</span><input type="month" {...register('budgetMonth', { required: 'Budget month is required' })} disabled={editing} className="w-full rounded-xl border border-slate-200 px-3 py-3 text-sm disabled:bg-slate-100" />{errors.budgetMonth && <span className="mt-1 block text-xs font-medium text-rose-600">{errors.budgetMonth.message}</span>}</label><div className="flex justify-end gap-3 pt-2"><button type="button" onClick={onClose} disabled={loading} className="rounded-xl px-4 py-2.5 text-sm font-semibold text-slate-600">Cancel</button><button type="submit" disabled={loading || !categories.length} className="rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-semibold text-white disabled:opacity-60">{loading ? 'Saving…' : editing ? 'Save changes' : 'Create budget'}</button></div>{!categories.length && <p className="text-xs font-medium text-amber-700">Create an expense category before creating a budget.</p>}</form></section></div>
}

export default BudgetModal
