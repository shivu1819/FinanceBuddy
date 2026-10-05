import { ArrowPathIcon } from '@heroicons/react/24/outline'

function Button({ children, loading = false, className = '', type = 'button', ...props }) {
  return (
    <button
      type={type}
      disabled={loading}
      className={`inline-flex w-full items-center justify-center gap-2 rounded-xl bg-slate-950 px-4 py-3.5 text-sm font-semibold text-white shadow-lg shadow-slate-950/15 transition duration-200 hover:bg-slate-800 focus:outline-none focus:ring-4 focus:ring-emerald-500/20 disabled:cursor-not-allowed disabled:opacity-70 ${className}`}
      {...props}
    >
      {loading && <ArrowPathIcon className="size-5 animate-spin" aria-hidden="true" />}
      {children}
    </button>
  )
}

export default Button
