import { PencilSquareIcon, TrashIcon } from '@heroicons/react/24/outline'
import CategoryIcon from './CategoryIcon'

function CategoryCard({ category, onEdit, onDelete }) {
  const count = category.transactionCount ?? category.transactionsCount ?? category.transactions?.length
  return <article className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm shadow-slate-900/3 md:hidden"><div className="flex items-start justify-between gap-3"><div className="flex items-center gap-3"><CategoryIcon icon={category.icon} color={category.color} /><div><h3 className="font-semibold text-slate-800">{category.name}</h3><span className={`mt-1 inline-block rounded-full px-2.5 py-1 text-xs font-bold ${category.type === 'INCOME' ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700'}`}>{category.type === 'INCOME' ? 'Income' : 'Expense'}</span></div></div><div className="flex gap-1"><button onClick={() => onEdit(category)} className="rounded-lg p-2 text-slate-500 hover:bg-slate-100" aria-label={`Edit ${category.name}`}><PencilSquareIcon className="size-5" /></button><button onClick={() => onDelete(category)} className="rounded-lg p-2 text-rose-500 hover:bg-rose-50" aria-label={`Delete ${category.name}`}><TrashIcon className="size-5" /></button></div></div><div className="mt-4 flex items-center justify-between border-t border-slate-100 pt-3 text-xs text-slate-500"><span className="inline-flex items-center gap-2"><i className="size-3 rounded-full" style={{ backgroundColor: category.color || '#64748b' }} />{category.color || 'No color set'}</span><span>{count === undefined ? 'Transaction count unavailable' : `${count} transaction${count === 1 ? '' : 's'}`}</span></div></article>
}

export default CategoryCard
