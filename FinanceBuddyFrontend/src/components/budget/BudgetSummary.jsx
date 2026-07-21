import { BanknotesIcon, ChartBarIcon, WalletIcon } from '@heroicons/react/24/outline'
import { formatCurrency } from '../../utils/dashboardData'

function BudgetSummary({ budget }) {
  const cards = [{ label: 'Total budget', value: formatCurrency(budget.monthlyLimit), icon: WalletIcon, tone: 'bg-emerald-100 text-emerald-700' }, { label: 'Total spent', value: formatCurrency(budget.spent), icon: BanknotesIcon, tone: 'bg-rose-100 text-rose-700' }, { label: 'Remaining', value: formatCurrency(budget.remaining), icon: WalletIcon, tone: 'bg-sky-100 text-sky-700' }, { label: 'Utilization', value: `${Number(budget.percentageUsed) || 0}%`, icon: ChartBarIcon, tone: 'bg-violet-100 text-violet-700' }]
  return <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{cards.map(({ label, value, icon: Icon, tone }) => <article key={label} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm shadow-slate-900/3"><div className="flex items-start justify-between"><div><p className="text-sm font-medium text-slate-500">{label}</p><p className="mt-2 text-2xl font-bold tracking-tight text-slate-950">{value}</p></div><span className={`grid size-11 place-items-center rounded-xl ${tone}`}><Icon className="size-5" /></span></div></article>)}</section>
}

export default BudgetSummary
