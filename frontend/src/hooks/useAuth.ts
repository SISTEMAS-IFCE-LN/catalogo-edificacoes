import { useContext } from 'react'
import { AuthContext } from '@/components/auth/AuthContext'
import type { AuthContextValue } from '@/components/auth/AuthContext'

/**
 * Hook de autenticação — acesso ao contexto de auth.
 * Deve ser usado dentro de <AuthProvider>.
 */
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth deve ser usado dentro de <AuthProvider>')
  }
  return ctx
}
