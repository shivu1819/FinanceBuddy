import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import Sidebar from '../../components/dashboard/Sidebar'
import Navbar from '../../components/dashboard/Navbar'
import ChangePasswordModal from '../../components/profile/ChangePasswordModal'
import ProfileCard from '../../components/profile/ProfileCard'
import ProfileForm from '../../components/profile/ProfileForm'
import SecurityCard from '../../components/profile/SecurityCard'
import SettingsCard from '../../components/profile/SettingsCard'
import useAuth from '../../hooks/useAuth'
import { getProfile, updateProfile } from '../../services/profileService'

const preferenceKey = 'financebuddy_preferences'
const defaultPreferences = { theme: 'system', language: 'en', notifications: true }
const getPreferences = () => { try { return { ...defaultPreferences, ...JSON.parse(localStorage.getItem(preferenceKey)) } } catch { return defaultPreferences } }

function ProfileSkeleton() { return <div className="animate-pulse space-y-6"><div className="h-72 rounded-2xl bg-slate-200" /><div className="grid gap-6 xl:grid-cols-2"><div className="h-[34rem] rounded-2xl bg-slate-200" /><div className="h-[30rem] rounded-2xl bg-slate-200" /></div></div> }

function Profile() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [preferences, setPreferences] = useState(getPreferences)
  const [passwordModalOpen, setPasswordModalOpen] = useState(false)
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  useEffect(() => { let active = true; async function loadProfile() { try { const data = await getProfile(); if (active) setProfile(data) } catch (error) { if (active) toast.error(error.response?.data?.message || 'Unable to load your profile. Please try again.') } finally { if (active) setLoading(false) } } loadProfile(); return () => { active = false } }, [])

  const saveProfile = async (payload) => { setSaving(true); try { const updated = await updateProfile(payload); setProfile(updated); toast.success('Profile updated successfully.') } catch (error) { toast.error(error.response?.data?.message || 'Unable to update your profile.') } finally { setSaving(false) } }
  const savePreferences = () => { localStorage.setItem(preferenceKey, JSON.stringify(preferences)); toast.success('Preferences saved on this device.') }
  const handleLogout = () => { logout(); navigate('/login', { replace: true }); toast.success('You have been logged out.') }
  const handlePasswordChange = () => { setPasswordModalOpen(false); toast.error('Password changes are not available from the current backend API.') }

  return <div className="flex min-h-screen bg-slate-50"><Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} /><div className="min-w-0 flex-1"><Navbar onMenuClick={() => setSidebarOpen(true)} user={user} /><main className="mx-auto max-w-7xl space-y-6 p-5 sm:p-8">{loading ? <ProfileSkeleton /> : profile && <><section><p className="text-sm font-medium text-emerald-600">Account settings</p><h2 className="mt-1 text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">Profile & settings</h2><p className="mt-2 text-sm text-slate-500">Manage your personal details, preferences, and account security.</p></section><ProfileCard profile={profile} email={user?.email || 'Not available'} /><section className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]"><ProfileForm profile={profile} loading={saving} onSubmit={saveProfile} /><div className="space-y-6"><SettingsCard settings={preferences} onChange={(key, value) => setPreferences((current) => ({ ...current, [key]: value }))} onSave={savePreferences} saving={saving} /><SecurityCard onChangePassword={() => setPasswordModalOpen(true)} onLogout={handleLogout} /></div></section></>}</main></div>{passwordModalOpen && <ChangePasswordModal onClose={() => setPasswordModalOpen(false)} onSubmit={handlePasswordChange} />}</div>
}
export default Profile
