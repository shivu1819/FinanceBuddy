import { useState } from 'react'
import { loginRequest, registerRequest } from '../services/authService'
import AuthContext from './authContext'

const ACCESS_TOKEN_KEY = 'financebuddy_access_token'
const USER_KEY = 'financebuddy_user'

const getStoredUser = () => {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY))
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

const getErrorMessage = (error, fallback) => {
  const data = error.response?.data

  if (typeof data === 'string') return data
  if (data?.message) return data.message
  if (data?.error) return data.error
  if (data?.errors && typeof data.errors === 'object') return Object.values(data.errors).flat().join(', ')

  if (error.code === 'ERR_NETWORK' || error.request) {
    return 'Unable to reach FinanceBuddy. Make sure the backend is running on port 8081 and try again.'
  }

  return fallback
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(ACCESS_TOKEN_KEY))
  const [user, setUser] = useState(getStoredUser)
  const [isInitializing] = useState(false)

  const saveSession = (response) => {
    const { accessToken, userId, fullName, email, role, emailVerified } = response
    const userInfo = { userId, fullName, email, role, emailVerified }

    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo))
    setToken(accessToken)
    setUser(userInfo)
  }

  const login = async (credentials) => {
    try {
      const { data } = await loginRequest(credentials)
      saveSession(data)
      return data
    } catch (error) {
      throw new Error(getErrorMessage(error, 'Unable to sign in. Please try again.'), { cause: error })
    }
  }

  const register = async (details) => {
    try {
      const { data } = await registerRequest(details)
      return data
    } catch (error) {
      throw new Error(getErrorMessage(error, 'Unable to create your account. Please try again.'), { cause: error })
    }
  }

  const logout = () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    setToken(null)
    setUser(null)
  }

  return <AuthContext value={{ token, user, isAuthenticated: Boolean(token), isInitializing, login, register, logout }}>{children}</AuthContext>
}
