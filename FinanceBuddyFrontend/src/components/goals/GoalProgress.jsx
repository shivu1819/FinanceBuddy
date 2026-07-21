function GoalProgress({ percentage }) {
  const value = Number(percentage) || 0
  return <div><div className="mb-2 flex justify-between text-xs font-semibold text-slate-500"><span>Progress</span><span>{value}%</span></div><div className="h-2.5 overflow-hidden rounded-full bg-slate-100"><div className="h-full rounded-full bg-violet-500 transition-all duration-700" style={{ width: `${Math.min(Math.max(value, 0), 100)}%` }} /></div></div>
}
export default GoalProgress
