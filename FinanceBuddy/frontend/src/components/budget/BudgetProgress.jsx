function getTone(percent) {
  if (percent > 90) return 'bg-rose-500'
  if (percent >= 70) return 'bg-amber-400'
  return 'bg-emerald-500'
}

function BudgetProgress({ percentage }) {
  const value = Number(percentage) || 0
  return <div><div className="mb-2 flex justify-between text-xs font-semibold text-slate-500"><span>Budget utilization</span><span>{value}%</span></div><div className="h-2.5 overflow-hidden rounded-full bg-slate-100"><div className={`h-full rounded-full transition-all ${getTone(value)}`} style={{ width: `${Math.min(Math.max(value, 0), 100)}%` }} /></div></div>
}

export default BudgetProgress
