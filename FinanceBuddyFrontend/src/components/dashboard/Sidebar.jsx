import {
  ArrowLeftStartOnRectangleIcon, ChartPieIcon, CreditCardIcon, FlagIcon,
  HomeIcon, TagIcon, UserCircleIcon, WalletIcon, XMarkIcon,
} from '@heroicons/react/24/outline'
import { NavLink, useNavigate } from 'react-router-dom'
import useAuth from '../../hooks/useAuth'

const navigation = [
  { label: 'Dashboard', to: '/dashboard', icon: HomeIcon }, { label: 'Transactions', to: '/transactions', icon: CreditCardIcon },
  { label: 'Categories', to: '/categories', icon: TagIcon }, { label: 'Budget', to: '/budget', icon: ChartPieIcon },
  { label: 'Goals', to: '/goals', icon: FlagIcon }, { label: 'Profile', to: '/profile', icon: UserCircleIcon },
]

function Sidebar({ open, onClose }) {
  const { logout } = useAuth(); const navigate = useNavigate()
  const handleLogout = () => { logout(); navigate('/login', { replace: true }) }
  return <><>{open && <button className="fixed inset-0 z-30 bg-slate-950/40 lg:hidden" onClick={onClose} aria-label="Close navigation" />}</><aside className={`fixed inset-y-0 left-0 z-40 flex w-72 flex-col bg-slate-950 p-5 text-slate-300 shadow-2xl transition-transform duration-300 lg:static lg:translate-x-0 lg:shadow-none ${open ? 'translate-x-0' : '-translate-x-full'}`}><div className="flex items-center justify-between px-2"><NavLink to="/dashboard" className="flex items-center gap-3 text-white" onClick={onClose}><span className="grid size-10 place-items-center rounded-xl bg-emerald-400 text-slate-950"><WalletIcon className="size-6" /></span><span className="text-xl font-bold tracking-tight">FinanceBuddy</span></NavLink><button onClick={onClose} className="rounded-lg p-2 text-slate-400 hover:bg-white/10 lg:hidden" aria-label="Close navigation"><XMarkIcon className="size-5" /></button></div><nav className="mt-10 space-y-1.5">{navigation.map(({ label, to, icon: Icon }) => <NavLink key={to} to={to} onClick={onClose} className={({ isActive }) => `flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition ${isActive ? 'bg-emerald-400 text-slate-950 shadow-lg shadow-emerald-500/10' : 'hover:bg-white/8 hover:text-white'}`}><Icon className="size-5" />{label}</NavLink>)}</nav><div className="mt-auto border-t border-white/10 pt-5"><button onClick={handleLogout} className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold text-slate-300 transition hover:bg-rose-500/10 hover:text-rose-300"><ArrowLeftStartOnRectangleIcon className="size-5" /> Logout</button></div></aside></>
}
export default Sidebar
