import { PencilSquareIcon, TrashIcon } from '@heroicons/react/24/outline'
import { formatCurrency, formatTransactionDate } from '../../utils/dashboardData'

function TransactionCard({ transaction, onEdit, onDelete }) {
  const income = transaction.transactionType === 'INCOME'
  return <article className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm shadow-slate-900/3 md:hidden"><div className="flex items-start justify-between gap-3"><div className="min-w-0"><p className="truncate font-semibold text-slate-800">{transaction.description || 'Untitled transaction'}</p><p className="mt-1 text-xs text-slate-500">{transaction.category?.name || 'Uncategorized'} · {formatTransactionDate(transaction.transactionDate)}</p></div><p className={`shrink-0 text-sm font-bold ${income ? 'text-emerald-600' : 'text-slate-800'}`}>{income ? '+' : '-'}{formatCurrency(transaction.amount)}</p></div><div className="mt-4 flex items-center justify-between"><span className={`rounded-full px-2.5 py-1 text-xs font-bold ${income ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700'}`}>{income ? 'Income' : 'Expense'}</span><div className="flex gap-1"><button onClick={() => onEdit(transaction)} className="rounded-lg p-2 text-slate-500 hover:bg-slate-100" aria-label="Edit transaction"><PencilSquareIcon className="size-5" /></button><button onClick={() => onDelete(transaction)} className="rounded-lg p-2 text-rose-500 hover:bg-rose-50" aria-label="Delete transaction"><TrashIcon className="size-5" /></button></div></div></article>
}

export default TransactionCard
