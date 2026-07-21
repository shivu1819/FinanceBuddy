import { useEffect, useMemo, useState } from 'react'
import { PlusIcon } from '@heroicons/react/24/outline'
import toast from 'react-hot-toast'
import Sidebar from '../../components/dashboard/Sidebar'
import Navbar from '../../components/dashboard/Navbar'
import TransactionFilters from '../../components/transactions/TransactionFilters'
import TransactionTable from '../../components/transactions/TransactionTable'
import TransactionCard from '../../components/transactions/TransactionCard'
import TransactionModal from '../../components/transactions/TransactionModal'
import DeleteDialog from '../../components/transactions/DeleteDialog'
import useAuth from '../../hooks/useAuth'
import { createTransaction, deleteTransaction, getCategories, getTransactions, updateTransaction } from '../../services/transactionService'

const defaultFilters = { search: '', type: '', category: '', date: '', sort: 'latest' }

function TransactionsSkeleton() {
  return <div className="animate-pulse space-y-5"><div className="h-28 rounded-2xl bg-slate-200" /><div className="h-16 rounded-2xl bg-slate-200" /><div className="h-96 rounded-2xl bg-slate-200" /></div>
}

function Transactions() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [transactions, setTransactions] = useState([])
  const [categories, setCategories] = useState([])
  const [filters, setFilters] = useState(defaultFilters)
  const [loading, setLoading] = useState(true)
  const [modalTransaction, setModalTransaction] = useState(undefined)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const { user } = useAuth()

  useEffect(() => {
    let active = true

    async function loadData() {
      try {
        const [transactionData, categoryData] = await Promise.all([getTransactions(), getCategories()])
        if (active) {
          setTransactions(transactionData)
          setCategories(categoryData)
        }
      } catch (error) {
        if (active) toast.error(error.response?.data?.message || 'Unable to load transactions. Please try again.')
      } finally {
        if (active) setLoading(false)
      }
    }

    loadData()
    return () => { active = false }
  }, [])

  const filteredTransactions = useMemo(() => {
    const query = filters.search.trim().toLowerCase()
    return transactions.filter((transaction) => (!query || (transaction.description || '').toLowerCase().includes(query)) && (!filters.type || transaction.transactionType === filters.type) && (!filters.category || String(transaction.category?.id) === filters.category) && (!filters.date || transaction.transactionDate === filters.date)).sort((a, b) => {
      if (filters.sort === 'oldest') return new Date(a.transactionDate) - new Date(b.transactionDate)
      if (filters.sort === 'highest') return Number(b.amount) - Number(a.amount)
      if (filters.sort === 'lowest') return Number(a.amount) - Number(b.amount)
      return new Date(b.transactionDate) - new Date(a.transactionDate)
    })
  }, [transactions, filters])

  const saveTransaction = async (payload) => {
    setSubmitting(true)
    try {
      if (modalTransaction?.id) {
        const updated = await updateTransaction(modalTransaction.id, payload)
        setTransactions((items) => items.map((item) => item.id === updated.id ? updated : item))
        toast.success('Transaction updated successfully.')
      } else {
        const created = await createTransaction(payload)
        setTransactions((items) => [created, ...items])
        toast.success('Transaction added successfully.')
      }
      setModalTransaction(undefined)
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to save the transaction.')
    } finally {
      setSubmitting(false)
    }
  }

  const confirmDelete = async () => {
    if (!deleteTarget) return
    setSubmitting(true)
    try {
      await deleteTransaction(deleteTarget.id)
      setTransactions((items) => items.filter((item) => item.id !== deleteTarget.id))
      setDeleteTarget(null)
      toast.success('Transaction deleted successfully.')
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to delete the transaction.')
    } finally {
      setSubmitting(false)
    }
  }

  return <div className="flex min-h-screen bg-slate-50"><Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} /><div className="min-w-0 flex-1"><Navbar onMenuClick={() => setSidebarOpen(true)} user={user} /><main className="mx-auto max-w-7xl space-y-6 p-5 sm:p-8">{loading ? <TransactionsSkeleton /> : <><section className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end"><div><p className="text-sm font-medium text-emerald-600">Money management</p><h2 className="mt-1 text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">Transactions</h2><p className="mt-2 text-sm text-slate-500">Review and manage every money movement in one place.</p></div><button onClick={() => setModalTransaction(null)} className="inline-flex items-center justify-center gap-2 rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-emerald-600/15 transition hover:bg-emerald-700"><PlusIcon className="size-5" /> Add transaction</button></section><TransactionFilters filters={filters} categories={categories} onChange={(key, value) => setFilters((current) => ({ ...current, [key]: value }))} />{filteredTransactions.length ? <><TransactionTable transactions={filteredTransactions} onEdit={setModalTransaction} onDelete={setDeleteTarget} /><div className="space-y-3 md:hidden">{filteredTransactions.map((transaction) => <TransactionCard key={transaction.id} transaction={transaction} onEdit={setModalTransaction} onDelete={setDeleteTarget} />)}</div></> : <section className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-20 text-center"><h3 className="text-lg font-bold text-slate-800">No transactions found</h3><p className="mx-auto mt-2 max-w-sm text-sm text-slate-500">{transactions.length ? 'Try changing your search or filters.' : 'Add your first income or expense to start tracking your finances.'}</p>{!transactions.length && <button onClick={() => setModalTransaction(null)} className="mt-5 text-sm font-semibold text-emerald-600 hover:text-emerald-700">Add transaction</button>}</section>}</> }</main></div>{modalTransaction !== undefined && <TransactionModal transaction={modalTransaction} categories={categories} loading={submitting} onClose={() => setModalTransaction(undefined)} onSubmit={saveTransaction} />}<DeleteDialog transaction={deleteTarget} loading={submitting} onCancel={() => setDeleteTarget(null)} onConfirm={confirmDelete} /></div>
}

export default Transactions
