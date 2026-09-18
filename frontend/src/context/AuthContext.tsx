import { createContext, useContext, useState, type ReactNode } from 'react'
import { api } from '../api/client'

export type Role = 'DISPATCHER' | 'TECHNICIAN' | 'MANAGER' | 'CUSTOMER'

interface AuthUser {
  email: string
  name: string
  role: Role
}

interface AuthContextValue {
  user: AuthUser | null
  login: (email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function loadStoredUser(): AuthUser | null {
  const raw = localStorage.getItem('keystone_user')
  return raw ? (JSON.parse(raw) as AuthUser) : null
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadStoredUser())

  async function login(email: string, password: string) {
    const { data } = await api.post('/auth/login', { email, password })
    const authUser: AuthUser = { email: data.email, name: data.name, role: data.role }
    localStorage.setItem('keystone_token', data.token)
    localStorage.setItem('keystone_user', JSON.stringify(authUser))
    setUser(authUser)
  }

  function logout() {
    localStorage.removeItem('keystone_token')
    localStorage.removeItem('keystone_user')
    setUser(null)
  }

  return <AuthContext.Provider value={{ user, login, logout }}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}