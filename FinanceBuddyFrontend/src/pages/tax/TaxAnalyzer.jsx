import { useState } from 'react'
import { CalculatorIcon, CheckCircleIcon, ScaleIcon } from '@heroicons/react/24/outline'
import toast from 'react-hot-toast'
import Sidebar from '../../components/dashboard/Sidebar'
import Navbar from '../../components/dashboard/Navbar'
import useAuth from '../../hooks/useAuth'
import { calculateTax, compareTaxRegimes } from '../../services/taxService'

const initialForm = { annualIncome: '', otherIncome: '', section80C: '', section80D: '', nps: '', homeLoanInterest: '', hra: '', professionalTax: '', regime: 'NEW' }
const fields = [{ key: 'annualIncome', label: 'Annual income', required: true }, { key: 'otherIncome', label: 'Other income' }]
const deductions = [{ key: 'section80C', label: 'Section 80C' }, { key: 'section80D', label: 'Section 80D' }, { key: 'nps', label: 'NPS' }, { key: 'homeLoanInterest', label: 'Home loan interest' }, { key: 'hra', label: 'HRA' }, { key: 'professionalTax', label: 'Professional tax' }]
const money = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 })
const amount = (value) => money.format(Number(value) || 0)

function getError(error) {
  const data = error.response?.data
  if (typeof data === 'string') return { message: data, fields: {} }
  if (data?.errors && typeof data.errors === 'object') return { message: data.message || 'Please correct the highlighted fields.', fields: data.errors }
  return { message: data?.message || data?.error || 'Unable to process your tax request. Please try again.', fields: {} }
}

function ResultCards({ result }) {
  if (!result) return null
  const cards = [{ label: 'Gross income', value: result.grossIncome }, { label: 'Taxable income', value: result.taxableIncome }, { label: 'Total deductions', value: result.totalDeductions }, { label: 'Tax before cess', value: result.taxBeforeCess }, { label: 'Cess', value: result.cess }, { label: 'Final tax', value: result.finalTax ?? result.totalTax }, { label: 'Monthly tax', value: result.monthlyTax }, { label: 'Effective tax rate', value: `${Number(result.effectiveTaxRate) || 0}%`, percent: true }]
  return <section><div className="mb-4"><h2 className="font-bold text-slate-950">Tax calculation</h2><p className="mt-1 text-sm text-slate-500">Estimated tax under the selected regime.</p></div><div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{cards.map((card) => <article key={card.label} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm shadow-slate-900/3"><p className="text-sm font-medium text-slate-500">{card.label}</p><p className="mt-2 text-xl font-bold tracking-tight text-slate-950">{card.percent ? card.value : amount(card.value)}</p></article>)}</div></section>
}

function RegimeCard({ title, data, better }) {
  if (!data) return null
  const tax = data.finalTax ?? data.totalTax ?? data.taxPayable
  return <article className={`rounded-2xl border p-5 shadow-sm ${better ? 'border-emerald-300 bg-emerald-50' : 'border-slate-200 bg-white'}`}><div className="flex items-center justify-between"><h3 className="font-bold text-slate-950">{title}</h3>{better && <span className="rounded-full bg-emerald-600 px-2.5 py-1 text-xs font-bold text-white">Better regime</span>}</div><p className="mt-5 text-sm text-slate-500">Estimated final tax</p><p className="mt-1 text-2xl font-bold text-slate-950">{amount(tax)}</p><div className="mt-5 space-y-2 text-sm text-slate-600"><p>Taxable income: <span className="font-semibold text-slate-800">{amount(data.taxableIncome)}</span></p><p>Total deductions: <span className="font-semibold text-slate-800">{amount(data.totalDeductions)}</span></p><p>Monthly tax: <span className="font-semibold text-slate-800">{amount(data.monthlyTax)}</span></p></div></article>
}

function TaxAnalyzer() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [form, setForm] = useState(initialForm)
  const [result, setResult] = useState(null)
  const [comparison, setComparison] = useState(null)
  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState({})
  const { user } = useAuth()
  const payload = Object.fromEntries(Object.entries(form).map(([key, value]) => [key, key === 'regime' ? value : value === '' ? 0 : Number(value)]))
  const update = (key, value) => { setForm((current) => ({ ...current, [key]: value })); setErrors((current) => ({ ...current, [key]: undefined })) }
  const run = async (action) => {
    if (!Number(payload.annualIncome)) { setErrors({ annualIncome: 'Annual income is required.' }); return }
    setLoading(true); setErrors({})
    try { if (action === 'calculate') { setResult(await calculateTax(payload)); toast.success('Tax calculated successfully.') } else { setComparison(await compareTaxRegimes(payload)); toast.success('Regimes compared successfully.') } } catch (error) { const parsed = getError(error); setErrors(parsed.fields); toast.error(parsed.message) } finally { setLoading(false) }
  }
  const better = comparison?.betterRegime || comparison?.recommendedRegime
  const saved = comparison?.taxSaved ?? comparison?.taxSaving
  const recommendation = comparison?.recommendation || (better ? `${better} regime is estimated to be more tax-efficient for your income and deductions.` : '')
  return <div className="flex min-h-screen bg-slate-50"><Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} /><div className="min-w-0 flex-1"><Navbar onMenuClick={() => setSidebarOpen(true)} user={user} /><main className="mx-auto max-w-7xl space-y-7 p-5 sm:p-8"><section><p className="text-sm font-medium text-emerald-600">Tax planning</p><h2 className="mt-1 text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">Smart tax analyzer</h2><p className="mt-2 text-sm text-slate-500">Estimate your income tax and compare the old and new regimes.</p></section><section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm shadow-slate-900/3 sm:p-6"><div className="grid gap-8 lg:grid-cols-2"><div><h3 className="font-bold text-slate-950">Income details</h3><div className="mt-4 grid gap-4 sm:grid-cols-2">{fields.map((field) => <label key={field.key}><span className="mb-1.5 block text-sm font-semibold text-slate-700">{field.label}</span><input type="number" min="0" step="0.01" value={form[field.key]} onChange={(event) => update(field.key, event.target.value)} className={`w-full rounded-xl border px-3 py-3 text-sm outline-none focus:ring-4 ${errors[field.key] ? 'border-rose-400 focus:ring-rose-100' : 'border-slate-200 focus:border-emerald-500 focus:ring-emerald-100'}`} />{errors[field.key] && <span className="mt-1 block text-xs font-medium text-rose-600">{Array.isArray(errors[field.key]) ? errors[field.key].join(', ') : errors[field.key]}</span>}</label>)}</div></div><div><h3 className="font-bold text-slate-950">Deductions</h3><div className="mt-4 grid gap-4 sm:grid-cols-2">{deductions.map((field) => <label key={field.key}><span className="mb-1.5 block text-sm font-semibold text-slate-700">{field.label}</span><input type="number" min="0" step="0.01" value={form[field.key]} onChange={(event) => update(field.key, event.target.value)} className={`w-full rounded-xl border px-3 py-3 text-sm outline-none focus:ring-4 ${errors[field.key] ? 'border-rose-400 focus:ring-rose-100' : 'border-slate-200 focus:border-emerald-500 focus:ring-emerald-100'}`} />{errors[field.key] && <span className="mt-1 block text-xs font-medium text-rose-600">{Array.isArray(errors[field.key]) ? errors[field.key].join(', ') : errors[field.key]}</span>}</label>)}</div></div></div><div className="mt-8 border-t border-slate-100 pt-6"><h3 className="font-bold text-slate-950">Tax regime</h3><div className="mt-4 flex flex-wrap gap-3">{[{ value: 'OLD', label: 'Old regime' }, { value: 'NEW', label: 'New regime' }].map((regime) => <label key={regime.value} className={`flex cursor-pointer items-center gap-2 rounded-xl border px-4 py-3 text-sm font-semibold ${form.regime === regime.value ? 'border-emerald-500 bg-emerald-50 text-emerald-800' : 'border-slate-200 text-slate-600'}`}><input type="radio" name="regime" value={regime.value} checked={form.regime === regime.value} onChange={(event) => update('regime', event.target.value)} />{regime.label}</label>)}</div><div className="mt-6 flex flex-col gap-3 sm:flex-row"><button onClick={() => run('calculate')} disabled={loading} className="inline-flex items-center justify-center gap-2 rounded-xl bg-emerald-600 px-5 py-3 text-sm font-semibold text-white hover:bg-emerald-700 disabled:opacity-60"><CalculatorIcon className={`size-5 ${loading ? 'animate-spin' : ''}`} />{loading ? 'Processing…' : 'Calculate tax'}</button><button onClick={() => run('compare')} disabled={loading} className="inline-flex items-center justify-center gap-2 rounded-xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-60"><ScaleIcon className="size-5" />Compare regimes</button></div></div></section><ResultCards result={result} />{comparison && <section><div className="mb-4"><h2 className="font-bold text-slate-950">Regime comparison</h2><p className="mt-1 text-sm text-slate-500">Choose the approach that best fits your financial situation.</p></div><div className="grid gap-5 lg:grid-cols-2"><RegimeCard title="Old regime" data={comparison.oldRegime ?? comparison.oldRegimeResult} better={better === 'OLD'} /><RegimeCard title="New regime" data={comparison.newRegime ?? comparison.newRegimeResult} better={better === 'NEW'} /></div><article className="mt-5 rounded-2xl border border-emerald-200 bg-emerald-50 p-5"><div className="flex items-start gap-3"><CheckCircleIcon className="mt-0.5 size-6 shrink-0 text-emerald-700" /><div><h3 className="font-bold text-emerald-950">Recommendation</h3>{saved !== undefined && <p className="mt-2 text-sm font-semibold text-emerald-800">Estimated tax saved: {amount(saved)}</p>}<p className="mt-1 text-sm leading-6 text-emerald-800">{recommendation}</p></div></div></article></section>}</main></div></div>
}

export default TaxAnalyzer
