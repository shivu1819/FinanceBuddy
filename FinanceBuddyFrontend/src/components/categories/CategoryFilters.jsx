import { MagnifyingGlassIcon } from '@heroicons/react/24/outline'

function CategoryFilters({ filters, onChange }) {
  return <section className="flex flex-col gap-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm shadow-slate-900/3 sm:flex-row"><label className="relative flex-1"><MagnifyingGlassIcon className="pointer-events-none absolute left-3 top-1/2 size-5 -translate-y-1/2 text-slate-400" /><input value={filters.search} onChange={(event) => onChange('search', event.target.value)} placeholder="Search categories" className="w-full rounded-xl border border-slate-200 py-2.5 pl-10 pr-3 text-sm outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100" /></label><select value={filters.type} onChange={(event) => onChange('type', event.target.value)} className="rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-700 outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100"><option value="">All types</option><option value="INCOME">Income</option><option value="EXPENSE">Expense</option></select></section>
}

export default CategoryFilters
