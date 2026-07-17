import { EnvelopeIcon, LockClosedIcon } from '@heroicons/react/24/outline'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { useState } from 'react'
import AuthLayout from '../../components/auth/AuthLayout'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import useAuth from '../../hooks/useAuth'

function Login() {
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const { register, handleSubmit, formState: { errors } } = useForm({ defaultValues: { email: '', password: '', remember: false } })

  const onSubmit = async (credentials) => {
    setLoading(true)
    try {
      await login({ email: credentials.email, password: credentials.password })
      toast.success('Welcome back to FinanceBuddy!')
      navigate(location.state?.from?.pathname || '/dashboard', { replace: true })
    } catch (error) {
      toast.error(error.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout eyebrow="Welcome back" title="Good to see you again." description="Sign in to stay on top of your financial world.">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5" noValidate>
        <Input label="Email address" name="email" type="email" placeholder="you@example.com" icon={EnvelopeIcon} register={register} error={errors.email} rules={{ required: 'Email address is required', pattern: { value: /^\S+@\S+\.\S+$/, message: 'Enter a valid email address' } }} />
        <Input label="Password" name="password" type="password" placeholder="Enter your password" icon={LockClosedIcon} register={register} error={errors.password} rules={{ required: 'Password is required', minLength: { value: 6, message: 'Password must be at least 6 characters' } }} />
        <div className="flex items-center justify-between gap-4">
          <label className="flex cursor-pointer items-center gap-2 text-sm text-slate-600"><input type="checkbox" className="size-4 rounded border-slate-300 text-emerald-600 focus:ring-emerald-500" {...register('remember')} /> Remember me</label>
          <button type="button" onClick={() => toast('Password recovery will be available soon.')} className="text-sm font-semibold text-emerald-600 hover:text-emerald-700">Forgot password?</button>
        </div>
        <Button type="submit" loading={loading}>Sign in to FinanceBuddy</Button>
      </form>
      <p className="mt-8 text-center text-sm text-slate-500">New to FinanceBuddy? <Link to="/register" className="font-semibold text-emerald-600 hover:text-emerald-700">Create an account</Link></p>
    </AuthLayout>
  )
}

export default Login
