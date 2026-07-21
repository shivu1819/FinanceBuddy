import { EnvelopeIcon, GlobeAltIcon, MapPinIcon, PhoneIcon } from '@heroicons/react/24/outline'
import AvatarUploader from './AvatarUploader'

function ProfileCard({ profile, email }) {
  const details = [{ label: 'Email', value: email, icon: EnvelopeIcon }, { label: 'Phone number', value: profile.phone || 'Not available', icon: PhoneIcon }, { label: 'Country', value: profile.country || 'Not set', icon: MapPinIcon }, { label: 'Time zone', value: profile.timeZone || Intl.DateTimeFormat().resolvedOptions().timeZone, icon: GlobeAltIcon }]
  return <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm shadow-slate-900/3 sm:p-6"><div className="flex flex-col gap-5 sm:flex-row sm:items-center"><AvatarUploader fullName={profile.fullName} /><div><h2 className="text-2xl font-bold text-slate-950">{profile.fullName}</h2><p className="mt-1 text-sm text-slate-500">{profile.occupation || 'FinanceBuddy member'}</p><p className="mt-2 text-xs font-medium text-emerald-600">Member since information is not available</p></div></div><div className="mt-6 grid gap-4 border-t border-slate-100 pt-6 sm:grid-cols-2">{details.map(({ label, value, icon: Icon }) => <div key={label} className="flex items-center gap-3"><span className="grid size-9 place-items-center rounded-xl bg-slate-100 text-slate-600"><Icon className="size-4" /></span><div className="min-w-0"><p className="text-xs font-medium text-slate-500">{label}</p><p className="truncate text-sm font-semibold text-slate-700">{value}</p></div></div>)}</div></section>
}
export default ProfileCard
