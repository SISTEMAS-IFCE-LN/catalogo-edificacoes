import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { RequireAuth } from './RequireAuth'

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
        <Route element={<RequireAuth />}>
          <Route path="/dashboard" element={<ProtectedChild />} />
        </Route>
        <Route path="/login" element={<p>página de login</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('RequireAuth', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('exibe loader quando isLoading é true', () => {
    mockUseAuth.mockReturnValue({ user: null, isAuthenticated: false, isLoading: true })
    const { container } = renderWithRouter('/dashboard')
    // FullScreenLoader renders a spinning div with border-b-2 border-primary
    const spinner = container.querySelector('.animate-spin')
    expect(spinner).toBeInTheDocument()
    expect(screen.queryByText('conteúdo protegido')).not.toBeInTheDocument()
    expect(screen.queryByText('página de login')).not.toBeInTheDocument()
  })

  it('redireciona para /login quando não autenticado', () => {
    mockUseAuth.mockReturnValue({ user: null, isAuthenticated: false, isLoading: false })
    renderWithRouter('/dashboard')
    expect(screen.getByText('página de login')).toBeInTheDocument()
    expect(screen.queryByText('conteúdo protegido')).not.toBeInTheDocument()
  })

  it('renderiza Outlet quando autenticado', () => {
    mockUseAuth.mockReturnValue({
      user: { id: 1, email: 'a@b.com', nome: 'A', ativo: true, criadoEm: '', perfis: [] },
      isAuthenticated: true,
      isLoading: false,
    })
    renderWithRouter('/dashboard')
    expect(screen.getByText('conteúdo protegido')).toBeInTheDocument()
    expect(screen.queryByText('página de login')).not.toBeInTheDocument()
  })
})
