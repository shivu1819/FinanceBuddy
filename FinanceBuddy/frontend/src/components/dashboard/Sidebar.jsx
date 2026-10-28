
import {
  ArrowLeftStartOnRectangleIcon,
  ChartBarSquareIcon,
  ChartPieIcon,
  CreditCardIcon,
  FlagIcon,
  CalculatorIcon,
  HomeIcon,
  TagIcon,
  UserCircleIcon,
  WalletIcon,
  XMarkIcon,
  ArrowTrendingUpIcon,
  CameraIcon,
  ArrowsRightLeftIcon,
  ArrowPathIcon,
  DocumentChartBarIcon,
} from '@heroicons/react/24/outline'

import { NavLink, useNavigate } from 'react-router-dom'
import useAuth from '../../hooks/useAuth'

const navigation = [
  { label: 'Dashboard', to: '/dashboard', icon: HomeIcon },
  { label: 'AI Insights', to: '/ai-insights', icon: ChartBarSquareIcon },
  { label: 'Tax Analyzer', to: '/tax-analyzer', icon: CalculatorIcon },
  { label: 'Transactions', to: '/transactions', icon: CreditCardIcon },
  { label: 'Categories', to: '/categories', icon: TagIcon },
  { label: 'Budget', to: '/budget', icon: ChartPieIcon },
  { label: 'Goals', to: '/goals', icon: FlagIcon },
  { label: 'Profile', to: '/profile', icon: UserCircleIcon },
  {
    label: 'Investment Planner',
    to: '/investments',
    icon: ArrowTrendingUpIcon,
  },
  { label: 'Receipt Scanner', to: '/receipts', icon: CameraIcon },
  {
    label: 'Bill Splitter',
    to: '/billsplit',
    icon: ArrowsRightLeftIcon,
  },
  {
    label: 'Recurring',
    to: '/recurring-transactions',
    icon: ArrowPathIcon,
  },
  { label: 'Reports', to: '/reports', icon: DocumentChartBarIcon },
]

function Sidebar({ open = false, onClose = () => {} }) {
  const { logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    onClose()
    navigate('/login', { replace: true })
  }

  return (
    <>
      {open && (
        <button
          type="button"
          className="fixed inset-0 z-30 bg-slate-950/40 lg:hidden"
          onClick={onClose}
          aria-label="Close navigation overlay"
        />
      )}

      <aside
        className={`fixed inset-y-0 left-0 z-40 flex w-72 flex-col bg-slate-950 p-5 text-slate-300 shadow-2xl transition-transform duration-300 lg:static lg:translate-x-0 lg:shadow-none ${
          open ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Brand */}
        <div className="flex items-center justify-between px-2">
          <NavLink
            to="/dashboard"
            className="flex items-center gap-3 text-white"
            onClick={onClose}
          >
            <span className="grid size-10 place-items-center rounded-xl bg-emerald-400 text-slate-950">
              <WalletIcon className="size-6" />
            </span>

            <span className="text-xl font-bold tracking-tight">
              FinanceBuddy
            </span>
          </NavLink>

          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-2 text-slate-400 hover:bg-white/10 lg:hidden"
            aria-label="Close navigation"
          >
            <XMarkIcon className="size-5" />
          </button>
        </div>

        {/* Navigation */}
        <nav className="mt-10 flex-1 space-y-1.5 overflow-y-auto">
          {navigation.map(({ label, to, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              onClick={onClose}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition ${
                  isActive
                    ? 'bg-emerald-400 text-slate-950 shadow-lg shadow-emerald-500/10'
                    : 'hover:bg-white/8 hover:text-white'
                }`
              }
            >
              <Icon className="size-5 shrink-0" />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>

        {/* Logout */}
        <div className="mt-5 border-t border-white/10 pt-5">
          <button
            type="button"
            onClick={handleLogout}
            className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold text-slate-300 transition hover:bg-rose-500/10 hover:text-rose-300"
          >
            <ArrowLeftStartOnRectangleIcon className="size-5" />
            Logout
          </button>
        </div>
      </aside>
    </>
  )
}

export default Sidebar
