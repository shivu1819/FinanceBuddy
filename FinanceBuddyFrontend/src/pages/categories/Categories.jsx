import { useEffect, useMemo, useState } from 'react'
import { PlusIcon } from '@heroicons/react/24/outline'
import toast from 'react-hot-toast'
import Sidebar from '../../components/dashboard/Sidebar'
import Navbar from '../../components/dashboard/Navbar'
import CategoryCard from '../../components/categories/CategoryCard'
import CategoryFilters from '../../components/categories/CategoryFilters'
import CategoryModal from '../../components/categories/CategoryModal'
import CategoryTable from '../../components/categories/CategoryTable'
import DeleteDialog from '../../components/categories/DeleteDialog'
import useAuth from '../../hooks/useAuth'
import { createCategory, deleteCategory, getCategories, updateCategory } from '../../services/categoryService'

function CategoriesSkeleton() {
  return <div className="animate-pulse space-y-5"><div className="h-28 rounded-2xl bg-slate-200" /><div className="h-16 rounded-2xl bg-slate-200" /><div className="h-96 rounded-2xl bg-slate-200" /></div>
}

function Categories() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [categories, setCategories] = useState([])
  const [filters, setFilters] = useState({ search: '', type: '' })
  const [loading, setLoading] = useState(true)
  const [modalCategory, setModalCategory] = useState(undefined)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const { user } = useAuth()

  useEffect(() => {
    let active = true
    async function loadCategories() {
      try {
        const data = await getCategories()
        if (active) setCategories(data)
      } catch (error) {
        if (active) toast.error(error.response?.data?.message || 'Unable to load categories. Please try again.')
      } finally {
        if (active) setLoading(false)
      }
    }
    loadCategories()
    return () => { active = false }
  }, [])

  const filteredCategories = useMemo(() => {
    const search = filters.search.trim().toLowerCase()
    return categories.filter((category) => (!search || category.name?.toLowerCase().includes(search)) && (!filters.type || category.type === filters.type))
  }, [categories, filters])

  const saveCategory = async (payload) => {
    setSubmitting(true)
    try {
      if (modalCategory?.id) {
        const updated = await updateCategory(modalCategory.id, payload)
        setCategories((items) => items.map((item) => item.id === updated.id ? updated : item))
        toast.success('Category updated successfully.')
      } else {
        const created = await createCategory(payload)
        setCategories((items) => [...items, created])
        toast.success('Category added successfully.')
      }
      setModalCategory(undefined)
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to save the category.')
    } finally {
      setSubmitting(false)
    }
  }

  const confirmDelete = async () => {
    if (!deleteTarget) return
    setSubmitting(true)
    try {
      await deleteCategory(deleteTarget.id)
      setCategories((items) => items.filter((item) => item.id !== deleteTarget.id))
      setDeleteTarget(null)
      toast.success('Category deleted successfully.')
    } catch (error) {
      toast.error(error.response?.data?.message || 'Unable to delete the category. It may still be in use.')
    } finally {
      setSubmitting(false)
    }
  }

  return <div className="flex min-h-screen bg-slate-50"><Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} /><div className="min-w-0 flex-1"><Navbar onMenuClick={() => setSidebarOpen(true)} user={user} /><main className="mx-auto max-w-7xl space-y-6 p-5 sm:p-8">{loading ? <CategoriesSkeleton /> : <><section className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end"><div><p className="text-sm font-medium text-emerald-600">Financial organization</p><h2 className="mt-1 text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">Categories</h2><p className="mt-2 text-sm text-slate-500">Create categories that make your financial activity easier to understand.</p></div><button onClick={() => setModalCategory(null)} className="inline-flex items-center justify-center gap-2 rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-emerald-600/15 transition hover:bg-emerald-700"><PlusIcon className="size-5" /> Add category</button></section><CategoryFilters filters={filters} onChange={(key, value) => setFilters((current) => ({ ...current, [key]: value }))} />{filteredCategories.length ? <><CategoryTable categories={filteredCategories} onEdit={setModalCategory} onDelete={setDeleteTarget} /><div className="space-y-3 md:hidden">{filteredCategories.map((category) => <CategoryCard key={category.id} category={category} onEdit={setModalCategory} onDelete={setDeleteTarget} />)}</div></> : <section className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-20 text-center"><h3 className="text-lg font-bold text-slate-800">No categories found</h3><p className="mx-auto mt-2 max-w-sm text-sm text-slate-500">{categories.length ? 'Try changing your search or filter.' : 'Create a category to start organizing your income and expenses.'}</p>{!categories.length && <button onClick={() => setModalCategory(null)} className="mt-5 text-sm font-semibold text-emerald-600 hover:text-emerald-700">Add category</button>}</section>}</>}</main></div>{modalCategory !== undefined && <CategoryModal category={modalCategory} loading={submitting} onClose={() => setModalCategory(undefined)} onSubmit={saveCategory} />}<DeleteDialog category={deleteTarget} loading={submitting} onCancel={() => setDeleteTarget(null)} onConfirm={confirmDelete} /></div>
}

export default Categories
