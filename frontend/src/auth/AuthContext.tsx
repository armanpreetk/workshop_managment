import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import axios from 'axios'
import { jwtDecode } from 'jwt-decode'

const API_BASE = import.meta.env.VITE_API_BASE as string

export type UserInfo = {
  id: number
  name: string
  type: 'ADMIN'|'MANAGER'|'EMPLOYEE'
  sub?: string
  iat?: number
  exp?: number
}

const api = axios.create({ baseURL: API_BASE })
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

interface AuthState {
  user: UserInfo | null
  token: string | null
  login: (name: string, password: string) => Promise<UserInfo>
  logout: () => void
}

const AuthCtx = createContext<AuthState | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'))
  const [user, setUser] = useState<UserInfo | null>(() => {
    const t = localStorage.getItem('token')
    if (!t) return null
  try { return jwtDecode<UserInfo>(t) } catch { return null }
  })

  useEffect(() => {
    if (token) localStorage.setItem('token', token); else localStorage.removeItem('token')
  }, [token])

  const login = async (name: string, password: string) => {
    const res = await api.post('/auth/login', { name, password })
    const t = res.data.token as string
    setToken(t)
  const decoded = jwtDecode<UserInfo>(t)
    setUser(decoded)
    return decoded
  }

  const logout = () => { setToken(null); setUser(null) }

  const value = useMemo(() => ({ user, token, login, logout }), [user, token])
  return <AuthCtx.Provider value={value}>{children}</AuthCtx.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthCtx)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}

export { api }
