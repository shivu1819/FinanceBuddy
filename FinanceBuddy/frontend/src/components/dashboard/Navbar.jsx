import { useEffect, useState } from 'react'
import { Bars3Icon, BellIcon, ChevronDownIcon } from '@heroicons/react/24/outline'
import { Link } from 'react-router-dom'
import { getUnreadCount } from '../../services/notificationService'

function Navbar({ onMenuClick, user }) {
  const [unread, setUnread] = useState(0)
  const firstName = user?.fullName?.split(' ')[0] || 'Alex'
  const initials = user?.fullName?.split(' ').map((word) => word[0]).join('').slice(0, 2).toUpperCase() || 'AB'
  useEffect(() => { getUnreadCount().then(setUnread).catch(() => {}) }, [])
  return <header className="flex items-center justify-between gap-4 border-b border-slate-200 bg-white px-5 py-4 sm:px-8"><div className="flex items-center gap-3"><button onClick={onMenuClick} className="rounded-xl p-2 text-slate-600 hover:bg-slate-100 lg:hidden" aria-label="Open navigation"><Bars3Icon className="size-6" /></button><div><p className="text-sm text-slate-500">Welcome back,</p><h1 className="text-lg font-bold tracking-tight text-slate-950 sm:text-xl">{firstName} <span aria-hidden="true">👋</span></h1></div></div><div className="flex items-center gap-3"><Link to="/notifications" className="relative rounded-xl p-2.5 text-slate-500 transition hover:bg-slate-100 hover:text-slate-800" aria-label="Notifications"><BellIcon className="size-5" />{unread > 0 && <span className="absolute -right-1 -top-1 min-w-5 rounded-full bg-rose-500 px-1 text-center text-[10px] font-bold text-white ring-2 ring-white">{unread > 9 ? '9+' : unread}</span>}</Link><button className="flex items-center gap-2 rounded-xl border border-slate-200 p-1.5 pr-2.5 transition hover:bg-slate-50" aria-label="Account menu"><span className="grid size-8 place-items-center rounded-lg bg-emerald-100 text-xs font-bold text-emerald-700">{initials}</span><ChevronDownIcon className="size-4 text-slate-500" /></button></div></header>
}
export default Navbar
