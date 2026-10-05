import { useForm } from 'react-hook-form'
import { XMarkIcon } from '@heroicons/react/24/outline'

const investmentTypes = [
  ['SIP', 'SIP'], ['MUTUAL_FUND', 'Mutual Fund'], ['STOCKS', 'Stocks'],
  ['FIXED_DEPOSIT', 'Fixed Deposit'], ['GOLD', 'Gold'], ['PPF', 'PPF'], ['NPS', 'NPS'], ['OTHER', 'Other'],
]
const riskLevels = [['LOW', 'Low'], ['MEDIUM', 'Medium'], ['HIGH', 'High']]

function FieldError({ error }) {
  return error ? <span className="mt-1 block text-xs font-medium text-rose-600">{error.message}</span> : null
}

function InvestmentModal({ investment, loading, onClose, onSubmit }) {
  const editing = Boolean(investment)
  const { register, handleSubmit, formState: { errors } } = useForm({
    defaultValues: investment ? {
      investmentName: investment.investmentName || '', investmentType: investment.investmentType || '',
      investedAmount: investment.investedAmount ?? '', currentValue: investment.currentValue ?? '',
      investmentDate: investment.investmentDate || '', expectedReturn: investment.expectedReturn ?? '',
      riskLevel: investment.riskLevel || '', notes: investment.notes || '',
    } : { investmentName: '', investmentType: '', investedAmount: '', currentValue: '', investmentDate: '', expectedReturn: '', riskLevel: '', notes: '' },
  })

  const submit = (values) => onSubmit({
    investmentName: values.investmentName.trim(), investmentType: values.investmentType,
    investedAmount: Number(values.investedAmount), currentValue: Number(values.currentValue),
    investmentDate: values.investmentDate, expectedReturn: Number(values.expectedReturn),
    riskLevel: values.riskLevel, notes: values.notes.trim() || null,
  })

  return <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-950/45 p-4 sm:grid sm:place-items-center" role="dialog" aria-modal="true" aria-labelledby="investment-modal-title">
    <section className="my-6 w-full max-w-2xl rounded-2xl bg-white p-5 shadow-2xl sm:my-0 sm:p-6">
      <div className="flex items-start justify-between gap-4"><div><h2 id="investment-modal-title" className="text-xl font-bold text-slate-950">{editing ? 'Edit investment' : 'Add investment'}</h2><p className="mt-1 text-sm text-slate-500">Track your investment without making assumptions about returns.</p></div><button onClick={onClose} disabled={loading} className="rounded-lg p-2 text-slate-500 hover:bg-slate-100" aria-label="Close investment modal"><XMarkIcon className="size-5" /></button></div>
      <form onSubmit={handleSubmit(submit)} className="mt-6 grid gap-4 sm:grid-cols-2">
        <label className="sm:col-span-2"><span className="mb-1.5 block text-sm font-semibold text-slate-700">Investment name</span><input {...register('investmentName', { required: 'Investment name is required' })} placeholder="e.g. Retirement SIP" className="w-full rounded-xl border border-slate-200 px-3 py-3 text-sm" /><FieldError error={errors.investmentName} /></label>
        <label><span className="mb-1.5 block text-sm font-semibold text-slate-700">Investment type</span><select {...register('investmentType', { required: 'Investment type is required' })} className="w-full rounded-xl border border-slate-200 bg-white px-3 py-3 text-sm"><option value="">Select type</option>{investmentTypes.map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select><FieldError error={errors.investmentType} /></label>
        <label><span className="mb-1.5 block text-sm font-semibold text-slate-700">Risk level</span><select {...register('riskLevel', { required: 'Risk level is required' })} className="w-full rounded-xl border border-slate-200 bg-white px-3 py-3 text-sm"><option value="">Select risk</option>{riskLevels.map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select><FieldError error={errors.riskLevel} /></label>
        <label><span className="mb-1.5 block text-sm font-semibold text-slate-700">Invested amount</span><input type="number" step="0.01" min="0.01" {...register('investedAmount', { required: 'Invested amount is required', valueAsNumber: true, min: { value: 0.01, message: 'Amount must be greater than zero' } })} placeholder="0.00" className="w-full rounded-xl border border-slate-200 px-3 py-3 text-sm" /><FieldError error={errors.investedAmount} /></label>
        <label><span className="mb-1.5 block text-sm font-semibold text-slate-700">Current value</span><input type="number" step="0.01" min="0" {...register('currentValue', { required: 'Current value is required', valueAsNumber: true, min: { value: 0, message: 'Current value cannot be negative' } })} placeholder="0.00" className="w-full rounded-xl border border-slate-200 px-3 py-3 text-sm" /><FieldError error={errors.currentValue} /></label>
        <label><span className="mb-1.5 block text-sm font-semibold text-slate-700">Investment date</span><input type="date" {...register('investmentDate', { required: 'Investment date is required' })} className="w-full rounded-xl border border-slate-200 px-3 py-3 text-sm" /><FieldError error={errors.investmentDate} /></label>
        <label><span className="mb-1.5 block text-sm font-semibold text-slate-700">Expected return (%)</span><input type="number" step="0.01" min="0" {...register('expectedReturn', { required: 'Expected return is required', valueAsNumber: true, min: { value: 0, message: 'Expected return cannot be negative' } })} placeholder="0.00" className="w-full rounded-xl border border-slate-200 px-3 py-3 text-sm" /><FieldError error={errors.expectedReturn} /></label>
        <label className="sm:col-span-2"><span className="mb-1.5 block text-sm font-semibold text-slate-700">Notes <span className="font-normal text-slate-400">(optional)</span></span><textarea {...register('notes')} rows="3" placeholder="Add a note about this investment" className="w-full resize-none rounded-xl border border-slate-200 px-3 py-3 text-sm" /></label>
        <div className="flex justify-end gap-3 sm:col-span-2"><button type="button" onClick={onClose} disabled={loading} className="rounded-xl px-4 py-2.5 text-sm font-semibold text-slate-600 hover:bg-slate-100">Cancel</button><button type="submit" disabled={loading} className="rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-emerald-700 disabled:opacity-60">{loading ? 'Saving…' : editing ? 'Save changes' : 'Add investment'}</button></div>
      </form>
    </section>
  </div>
}

export default InvestmentModal