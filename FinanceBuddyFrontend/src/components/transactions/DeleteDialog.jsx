import { ExclamationTriangleIcon } from '@heroicons/react/24/outline'

function DeleteDialog({ transaction, loading, onCancel, onConfirm }) {
  if (!transaction) return null
  return <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/45 p-4" role="dialog" aria-modal="true" aria-labelledby="delete-title"><section className="w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl"><span className="grid size-12 place-items-center rounded-xl bg-rose-100 text-rose-600"><ExclamationTriangleIcon className="size-6" /></span><h2 id="delete-title" className="mt-4 text-lg font-bold text-slate-950">Delete transaction?</h2><p className="mt-2 text-sm leading-6 text-slate-500">This will permanently remove “{transaction.description || 'Untitled transaction'}”. This action cannot be undone.</p><div className="mt-6 flex justify-end gap-3"><button disabled={loading} onClick={onCancel} className="rounded-xl px-4 py-2.5 text-sm font-semibold text-slate-600 hover:bg-slate-100 disabled:opacity-60">Cancel</button><button disabled={loading} onClick={onConfirm} className="rounded-xl bg-rose-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-rose-700 disabled:opacity-60">{loading ? 'Deleting…' : 'Delete'}</button></div></section></div>
}

export default DeleteDialog
