import { CameraIcon } from '@heroicons/react/24/outline'

function AvatarUploader({ fullName }) {
  const initials = fullName?.split(' ').map((name) => name[0]).join('').slice(0, 2).toUpperCase() || 'FB'
  return <div className="relative w-fit"><span className="grid size-24 place-items-center rounded-3xl bg-emerald-100 text-2xl font-bold text-emerald-700">{initials}</span><span title="Profile photo uploads are not available from the current API." className="absolute -bottom-2 -right-2 grid size-9 place-items-center rounded-xl border-4 border-white bg-slate-200 text-slate-500"><CameraIcon className="size-4" /></span></div>
}
export default AvatarUploader
