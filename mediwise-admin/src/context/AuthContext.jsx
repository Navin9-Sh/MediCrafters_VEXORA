import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { loginWithEmailPassword, logout as logoutService } from '../services/authService'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null)
  const [token, setToken]     = useState(null)
  const [loading, setLoading] = useState(true)

  // ── Restore session from localStorage on first load ────────────────────────
  useEffect(() => {
    const savedToken = localStorage.getItem('mediwise_admin_token')
    const savedUser  = localStorage.getItem('mediwise_admin_user')
    if (savedToken && savedUser) {
      setToken(savedToken)
      setUser(JSON.parse(savedUser))
    }
    setLoading(false)
  }, [])

  // ── Login ──────────────────────────────────────────────────────────────────
  const login = useCallback(async (email, password) => {
    const response = await loginWithEmailPassword(email, password)
    const { accessToken, user: userData } = response.data

    // Security: only allow ADMIN role users to access this panel
    if (userData.role !== 'ADMIN') {
      throw new Error('Access denied. Admin credentials required.')
    }

    localStorage.setItem('mediwise_admin_token', accessToken)
    localStorage.setItem('mediwise_admin_user', JSON.stringify(userData))
    setToken(accessToken)
    setUser(userData)
  }, [])

  // ── Logout ─────────────────────────────────────────────────────────────────
  const logout = useCallback(async () => {
    await logoutService()
    setToken(null)
    setUser(null)
  }, [])

  const isAuthenticated = !!token && !!user

  return (
    <AuthContext.Provider value={{ user, token, loading, isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
