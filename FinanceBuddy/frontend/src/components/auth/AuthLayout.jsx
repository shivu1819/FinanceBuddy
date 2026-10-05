import { ArrowTrendingUpIcon, ChartPieIcon, ShieldCheckIcon, WalletIcon } from '@heroicons/react/24/outline'

const benefits = [
  { icon: WalletIcon, text: 'All your accounts, beautifully organized' },
  { icon: ChartPieIcon, text: 'Smart insights that make every rupee count' },
  { icon: ShieldCheckIcon, text: 'Your financial data stays private and secure' },
]

function AuthLayout({ children, eyebrow, title, description }) {
  return (
    <main className="min-h-screen bg-[#f8fafc] p-4 sm:p-6 lg:p-8">
      <div className="mx-auto grid min-h-[calc(100vh-2rem)] max-w-6xl overflow-hidden rounded-3xl bg-white shadow-2xl shadow-slate-900/10 lg:grid-cols-[1.08fr_0.92fr] sm:min-h-[calc(100vh-3rem)]">
        <section className="relative hidden overflow-hidden bg-slate-950 p-10 text-white lg:flex lg:flex-col xl:p-14">
          <div className="absolute -left-28 top-24 size-72 rounded-full bg-emerald-500/20 blur-3xl" />
          <div className="absolute -right-20 bottom-0 size-80 rounded-full bg-sky-500/20 blur-3xl" />
          <div className="relative flex items-center gap-3">
            <span className="grid size-11 place-items-center rounded-xl bg-emerald-400 text-slate-950"><ArrowTrendingUpIcon className="size-6" /></span>
            <span className="text-xl font-bold tracking-tight">FinanceBuddy</span>
          </div>

          <div className="relative my-auto pt-16">
            <p className="mb-4 text-sm font-semibold uppercase tracking-[0.2em] text-emerald-300">Your money, made simple</p>
            <h2 className="max-w-md text-4xl font-bold leading-tight tracking-tight xl:text-5xl">Clarity for every financial decision.</h2>
            <p className="mt-6 max-w-md text-base leading-7 text-slate-300">Build better habits, plan with confidence, and feel in control of where your money goes.</p>
            <div className="mt-10 space-y-5">
              {benefits.map(({ icon: Icon, text }) => (
                <div key={text} className="flex items-center gap-3 text-sm font-medium text-slate-200">
                  <span className="grid size-9 place-items-center rounded-lg bg-white/10 text-emerald-300"><Icon className="size-5" /></span>
                  {text}
                </div>
              ))}
            </div>
          </div>
          <p className="relative text-sm text-slate-400">© {new Date().getFullYear()} FinanceBuddy. Better money days ahead.</p>
        </section>

        <section className="flex items-center justify-center px-5 py-10 sm:px-10 lg:px-12">
          <div className="w-full max-w-md">
            <div className="mb-10 flex items-center gap-3 lg:hidden">
              <span className="grid size-10 place-items-center rounded-xl bg-slate-950 text-emerald-400"><ArrowTrendingUpIcon className="size-5" /></span>
              <span className="text-lg font-bold tracking-tight text-slate-950">FinanceBuddy</span>
            </div>
            <p className="mb-3 text-sm font-semibold text-emerald-600">{eyebrow}</p>
            <h1 className="text-3xl font-bold tracking-tight text-slate-950 sm:text-4xl">{title}</h1>
            <p className="mt-3 text-sm leading-6 text-slate-500">{description}</p>
            <div className="mt-8">{children}</div>
          </div>
        </section>
      </div>
    </main>
  )
}

export default AuthLayout
