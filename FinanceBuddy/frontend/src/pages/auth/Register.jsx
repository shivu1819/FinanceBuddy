import { EnvelopeIcon, LockClosedIcon, UserIcon } from '@heroicons/react/24/outline'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { useState } from 'react'
import AuthLayout from '../../components/auth/AuthLayout'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import useAuth from '../../hooks/useAuth'

function Register() {
  const [loading, setLoading] = useState(false)
  const { register: registerAccount } = useAuth()
  const navigate = useNavigate()
  const { register, handleSubmit, getValues, formState: { errors } } = useForm()

  const onSubmit = async (values) => {
    setLoading(true)
    try {
      await registerAccount({ fullName: values.fullName, email: values.email, password: values.password })
      toast.success('Account created successfully. Please sign in.')
      navigate('/login', { replace: true })
    } catch (error) {
      toast.error(error.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout eyebrow="Start your journey" title="Take charge of your money." description="Create your free account and make every financial move count.">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <Input label="Full name" name="fullName" placeholder="Your full name" icon={UserIcon} register={register} error={errors.fullName} rules={{ required: 'Full name is required', minLength: { value: 2, message: 'Name must be at least 2 characters' }, maxLength: { value: 150, message: 'Name must not exceed 150 characters' } }} />
        <Input label="Email address" name="email" type="email" placeholder="you@example.com" icon={EnvelopeIcon} register={register} error={errors.email} rules={{ required: 'Email address is required', pattern: { value: /^\S+@\S+\.\S+$/, message: 'Enter a valid email address' } }} />
        <Input label="Password" name="password" type="password" placeholder="Create a password" icon={LockClosedIcon} register={register} error={errors.password} rules={{ required: 'Password is required', minLength: { value: 8, message: 'Use at least 8 characters' }, validate: (value) => /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/.test(value) || 'Include uppercase, lowercase, and a number' }} />
        <Input label="Confirm password" name="confirmPassword" type="password" placeholder="Repeat your password" icon={LockClosedIcon} register={register} error={errors.confirmPassword} rules={{ required: 'Please confirm your password', validate: (value) => value === getValues('password') || 'Passwords do not match' }} />
        <Button type="submit" loading={loading} className="mt-2">Create your account</Button>
      </form>
      <p className="mt-7 text-center text-sm text-slate-500">Already have an account? <Link to="/login" className="font-semibold text-emerald-600 hover:text-emerald-700">Sign in</Link></p>
    </AuthLayout>
  )
}

export default Register
