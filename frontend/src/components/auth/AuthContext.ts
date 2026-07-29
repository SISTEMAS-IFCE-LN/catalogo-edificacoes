import { createContext } from 'react'
import type { AuthState } from '@/types/user'

export interface AuthContextValue extends AuthState {
  login: (token: string) => Promise<void>
  logout: () => Promise<void>
  refreshUser: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)
