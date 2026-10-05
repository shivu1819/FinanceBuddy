import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './ProtectedRoute'
import Login from '../pages/auth/Login'
import Register from '../pages/auth/Register'
import Dashboard from '../pages/dashboard/Dashboard'
import Transactions from '../pages/transactions/Transactions'
import Categories from '../pages/categories/Categories'
import Budget from '../pages/budget/Budget'
import Goals from '../pages/goals/Goals'
import Profile from '../pages/profile/Profile'
import AIInsights from '../pages/insights/AIInsights'
import TaxAnalyzer from '../pages/tax/TaxAnalyzer'
import Investments from '../pages/investments/Investments'
import Receipts from '../pages/receipts/Receipts'
import BillSplit from '../pages/billsplit/BillSplit'
import RecurringTransactions from '../pages/recurring/RecurringTransactions'
import Notifications from '../pages/notifications/Notifications'
import Reports from '../pages/reports/Reports'
import NotFound from '../pages/NotFound'

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/ai-insights" element={<AIInsights />} />
        <Route path="/tax-analyzer" element={<TaxAnalyzer />} />
        <Route path="/transactions" element={<Transactions />} />
        <Route path="/categories" element={<Categories />} />
        <Route path="/budget" element={<Budget />} />
        <Route path="/goals" element={<Goals />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/investments" element={<Investments />} />
        <Route path="/receipts" element={<Receipts />} />
        <Route path="/bill-split" element={<BillSplit />} />
        <Route path="/recurring-transactions" element={<RecurringTransactions />} />
        <Route path="/notifications" element={<Notifications />} />
        <Route path="/reports" element={<Reports />} />
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}

export default AppRoutes
