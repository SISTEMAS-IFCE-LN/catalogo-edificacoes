import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { RequireRole } from './RequireRole'
import { Role } from '@/types/user'
import type { User } from '@/types/user'

const mockUseAuth = vi.fn()

vi.mock('@/hooks/useAuth', () => ({
  useAuth: () => mockUseAuth(),
}))

function ProtectedChild() {
  return <p>conteúdo protegido</p>
}

function renderWithRouter(initialPath: string) {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <Routes>
        <Route element={<RequireRole roles={[Role.COLABORADOR]} />}>
          <Route path="/protected" element={<ProtectedChild />} />
        </Route>
        <Route
          element={<RequireRole roles={[Role.ADMINISTRADOR]} />}
        >
          <Route path="/admin" element={<ProtectedChild />} />
        </Route>
        <Route path="/login" element={<p>página de login</p>} />
        <Route path="/unauthorized" element={<p>página não autorizada</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

const fakeUser: User = {
  id: 1,
  email: 'dev@ifce.edu.br',
  nome: 'Dev FakeAuth',
  ativo: true,
  criadoEm: new Date().toISOString(),
  perfis: [Role.COLABORADOR],
}

describe('RequireRole', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renderiza Outlet quando o usuário tem a role exigida', () => {
    mockUseAuth.mockReturnValue({ user: fakeUser, isAuthenticated: true, isLoading: false })
    renderWithRouter('/protected')
    expect(screen.getByText('conteúdo protegido')).toBeInTheDocument()
  })

  it('redireciona para /login quando user é null (não autenticado)', () => {
    mockUseAuth.mockReturnValue({ user: null, isAuthenticated: false, isLoading: false })
    renderWithRouter('/protected')
    expect(screen.getByText('página de login')).toBeInTheDocument()
    expect(screen.queryByText('conteúdo protegido')).not.toBeInTheDocument()
  })

  it('redireciona para /unauthorized quando o usuário não tem a role exigida', () => {
    mockUseAuth.mockReturnValue({ user: fakeUser, isAuthenticated: true, isLoading: false })
    renderWithRouter('/admin')
    expect(screen.getByText('página não autorizada')).toBeInTheDocument()
    expect(screen.queryByText('conteúdo protegido')).not.toBeInTheDocument()
  })
})
