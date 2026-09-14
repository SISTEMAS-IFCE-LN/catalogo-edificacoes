import { render, screen, act, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import type { User } from '@/types/usuarios/user'
import { Role } from '@/types/usuarios/user'

// --- Mocks de módulo (hoisted) ---

const mockApiGet = vi.fn()
const mockApiPost = vi.fn()
const mockRefreshAccessToken = vi.fn()

const mockEnsureCsrfToken = vi.fn()
const mockClearCsrfToken = vi.fn()

vi.mock('@/lib/api/api', () => ({
    api: {
        get: (...args: unknown[]) => mockApiGet(...args),
        post: (...args: unknown[]) => mockApiPost(...args),
    },
    refreshAccessToken: (...args: unknown[]) => mockRefreshAccessToken(...args),
}))

vi.mock('@/lib/security/csrf', () => ({
    ensureCsrfToken: (...args: unknown[]) => mockEnsureCsrfToken(...args),
    clearCsrfToken: (...args: unknown[]) => mockClearCsrfToken(...args),
}))

const mockSetAccessToken = vi.fn()
const mockGetAccessToken = vi.fn()
const mockClearAccessToken = vi.fn()

vi.mock('@/lib/security/auth', () => ({
  setAccessToken: (...args: unknown[]) => mockSetAccessToken(...args),
  getAccessToken: (...args: unknown[]) => mockGetAccessToken(...args),
  clearAccessToken: (...args: unknown[]) => mockClearAccessToken(...args),
}))

// --- Dados de teste ---

const realUser: User = {
  id: 42,
  email: 'real@ifce.edu.br',
  nome: 'Usuário Real',
  ativo: true,
  criadoEm: '2025-01-01T00:00:00.000Z',
  perfis: [Role.COLABORADOR],
}

// --- Helper: renderiza AuthProvider fresco com controle de VITE_FAKE_AUTH ---

async function renderWithFreshProvider(envFakeAuth: string) {
  vi.resetModules()
  vi.stubEnv('VITE_FAKE_AUTH', envFakeAuth)

  // Re-registra mocks após resetModules (vi.doMock não é hoisted)
    vi.doMock('@/lib/api/api', () => ({
        api: {
            get: (...args: unknown[]) => mockApiGet(...args),
            post: (...args: unknown[]) => mockApiPost(...args),
        },
        refreshAccessToken: (...args: unknown[]) => mockRefreshAccessToken(...args),
    }))

    vi.doMock('@/lib/security/csrf', () => ({
        ensureCsrfToken: (...args: unknown[]) => mockEnsureCsrfToken(...args),
        clearCsrfToken: (...args: unknown[]) => mockClearCsrfToken(...args),
    }))

  vi.doMock('@/lib/security/auth', () => ({
    setAccessToken: (...args: unknown[]) => mockSetAccessToken(...args),
    getAccessToken: (...args: unknown[]) => mockGetAccessToken(...args),
    clearAccessToken: (...args: unknown[]) => mockClearAccessToken(...args),
  }))

  // Importa AuthProvider do módulo fresco
  const authModule = await import('./AuthProvider')
  const { AuthProvider } = authModule
  // Importa useAuth do hook (que usa o AuthContext do módulo fresco)
  const { useAuth } = await import('@/hooks/useAuth')

  // Componente consumidor que usa o useAuth do módulo fresco
  function Consumer() {
    const { user, isAuthenticated, isLoading, login, logout, refreshUser } = useAuth()
    return (
      <div>
        <span data-testid="c-loading">{String(isLoading)}</span>
        <span data-testid="c-authenticated">{String(isAuthenticated)}</span>
        <span data-testid="c-user">{user ? user.nome : 'null'}</span>
        <button data-testid="c-login" onClick={() => { void login('tok-123') }}>login</button>
        <button data-testid="c-logout" onClick={() => { void logout() }}>logout</button>
        <button data-testid="c-refresh" onClick={() => { void refreshUser() }}>refresh</button>
      </div>
    )
  }

  const utils = render(
    <AuthProvider><Consumer /></AuthProvider>,
  )
  return { ...utils, useAuth }
}

// --- Testes ---

describe('AuthProvider', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockGetAccessToken.mockReturnValue(null)
  })

  afterEach(() => {
    vi.unstubAllEnvs()
    vi.resetModules()
  })

  describe('boot com FakeAuth', () => {
    it('define usuário fake imediatamente sem chamar API', async () => {
      await renderWithFreshProvider('true')

      await waitFor(() => {
        expect(screen.getByTestId('c-loading')).toHaveTextContent('false')
      })
      expect(screen.getByTestId('c-authenticated')).toHaveTextContent('true')
      expect(screen.getByTestId('c-user')).toHaveTextContent('Dev FakeAuth')
      expect(mockApiGet).not.toHaveBeenCalled()
    })
  })

  describe('boot sem FakeAuth', () => {
    it('chama refreshAccessToken e carrega usuário via GET /api/usuarios/me', async () => {
      mockRefreshAccessToken.mockResolvedValueOnce('new-jwt')
      mockApiGet.mockResolvedValueOnce({ data: realUser })

      await renderWithFreshProvider('false')

      await waitFor(() => {
        expect(screen.getByTestId('c-loading')).toHaveTextContent('false')
      })
      expect(mockRefreshAccessToken).toHaveBeenCalledTimes(1)
      expect(mockApiGet).toHaveBeenCalledWith('/api/usuarios/me')
      expect(screen.getByTestId('c-authenticated')).toHaveTextContent('true')
      expect(screen.getByTestId('c-user')).toHaveTextContent('Usuário Real')
    })

    it('em falha do refresh, limpa estado e marca não autenticado', async () => {
      mockRefreshAccessToken.mockRejectedValueOnce(new Error('refresh failed'))

      await act(async () => {
        await renderWithFreshProvider('false')
      })

      await waitFor(() => {
        expect(screen.getByTestId('c-loading')).toHaveTextContent('false')
      })
      expect(mockClearAccessToken).toHaveBeenCalled()
      expect(screen.getByTestId('c-authenticated')).toHaveTextContent('false')
      expect(screen.getByTestId('c-user')).toHaveTextContent('null')
    })
  })

  describe('login', () => {
    it('define o token e carrega o usuário', async () => {
      // Boot com FakeAuth para ter um estado inicial estável
      await renderWithFreshProvider('true')

      await waitFor(() => {
        expect(screen.getByTestId('c-loading')).toHaveTextContent('false')
      })

      // Prepara mock para o loadUser chamado pelo login
      mockApiGet.mockResolvedValueOnce({ data: realUser })

      await act(async () => {
        screen.getByTestId('c-login').click()
      })

      expect(mockSetAccessToken).toHaveBeenCalledWith('tok-123')
      await waitFor(() => {
        expect(mockApiGet).toHaveBeenCalledWith('/api/usuarios/me')
      })
      expect(screen.getByTestId('c-user')).toHaveTextContent('Usuário Real')
    })
  })

  describe('logout', () => {
    it('limpa estado local ao fazer logout', async () => {
      await renderWithFreshProvider('true')

      await waitFor(() => {
        expect(screen.getByTestId('c-authenticated')).toHaveTextContent('true')
      })

      await act(async () => {
        screen.getByTestId('c-logout').click()
      })

      await waitFor(() => {
        expect(screen.getByTestId('c-authenticated')).toHaveTextContent('false')
        expect(screen.getByTestId('c-user')).toHaveTextContent('null')
      })
      expect(mockClearAccessToken).toHaveBeenCalled()
    })

    it('chama POST /auth/logout quando FakeAuth está desligado', async () => {
      // Boot sem FakeAuth
      mockRefreshAccessToken.mockResolvedValueOnce('new-jwt')
      mockApiGet.mockResolvedValueOnce({ data: realUser })
      mockApiPost.mockResolvedValueOnce({})

      await renderWithFreshProvider('false')

      await waitFor(() => {
        expect(screen.getByTestId('c-authenticated')).toHaveTextContent('true')
      })

      await act(async () => {
        screen.getByTestId('c-logout').click()
      })

      // Verifica que POST /auth/logout foi chamado (endpoint correto)
      await waitFor(() => {
        expect(mockApiPost).toHaveBeenCalledWith('/auth/logout')
      })
      expect(mockClearAccessToken).toHaveBeenCalled()
      expect(screen.getByTestId('c-authenticated')).toHaveTextContent('false')
    })

    it('NÃO chama backend quando FakeAuth está ligado', async () => {
      await renderWithFreshProvider('true')

      await waitFor(() => {
        expect(screen.getByTestId('c-authenticated')).toHaveTextContent('true')
      })

      await act(async () => {
        screen.getByTestId('c-logout').click()
      })

      // Com FakeAuth, não deve chamar o backend
      expect(mockApiPost).not.toHaveBeenCalled()
      expect(mockClearAccessToken).toHaveBeenCalled()
    })
  })

  describe('evento auth:logout', () => {
    it('limpa estado ao receber evento auth:logout', async () => {
      await renderWithFreshProvider('true')

      await waitFor(() => {
        expect(screen.getByTestId('c-authenticated')).toHaveTextContent('true')
      })

      await act(async () => {
        window.dispatchEvent(new CustomEvent('auth:logout'))
      })

      await waitFor(() => {
        expect(screen.getByTestId('c-authenticated')).toHaveTextContent('false')
        expect(screen.getByTestId('c-user')).toHaveTextContent('null')
      })
      expect(mockClearAccessToken).toHaveBeenCalled()
    })
  })
})
