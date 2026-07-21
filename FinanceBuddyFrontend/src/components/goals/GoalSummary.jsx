import { BanknotesIcon, CheckCircleIcon, FlagIcon, RocketLaunchIcon } from '@heroicons/react/24/outline'
import { formatCurrency } from '../../utils/dashboardData'

function GoalSummary({ goals }) {
  const active = goals.filter((goal) => goal.status !== 'COMPLETED').length
  const completed = goals.length - active
  const saved = goals.reduce((total, goal) => total + (Number(goal.savedAmount) || 0), 0)
  const cards = [{ label: 'Total goals', value: goals.length, icon: FlagIcon, tone: 'bg-violet-100 text-violet-700' }, { label: 'Active goals', value: active, icon: RocketLaunchIcon, tone: 'bg-sky-100 text-sky-700' }, { label: 'Completed goals', value: completed, icon: CheckCircleIcon, tone: 'bg-emerald-100 text-emerald-700' }, { label: 'Total saved', value: formatCurrency(saved), icon: BanknotesIcon, tone: 'bg-amber-100 text-amber-700' }]
  return <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{cards.map(({ label, value, icon: Icon, tone }) => <article key={label} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm shadow-slate-900/3"><div className="flex items-start justify-between"><div><p className="text-sm font-medium text-slate-500">{label}</p><p className="mt-2 text-2xl font-bold tracking-tight text-slate-950">{value}</p></div><span className={`grid size-11 place-items-center rounded-xl ${tone}`}><Icon className="size-5" /></span></div></article>)}</section>
}
export default GoalSummary
