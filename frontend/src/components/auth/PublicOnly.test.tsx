import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { PublicOnly } from './PublicOnly'

const mockUseAuth = vi.fn()

vi.mock('@/hooks/useAuth', () => ({
  useAuth: () => mockUseAuth(),
}))

function PublicChild() {
  return <p>página pública</p>
}

function renderWithRouter(initialPath: string) {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <Routes>
        <Route element={<PublicOnly />}>
          <Route path="/login" element={<PublicChild />} />
        </Route>
        <Route path="/" element={<p>página inicial</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('PublicOnly', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renderiza null quando isLoading é true', () => {
    mockUseAuth.mockReturnValue({ user: null, isAuthenticated: false, isLoading: true })
    const { container } = renderWithRouter('/login')
    // PublicOnly returns null during loading — no child content, no redirect
    expect(container.innerHTML).toBe('')
    expect(screen.queryByText('página pública')).not.toBeInTheDocument()
    expect(screen.queryByText('página inicial')).not.toBeInTheDocument()
  })

  it('redireciona para / quando autenticado', () => {
    mockUseAuth.mockReturnValue({
      user: { id: 1, email: 'a@b.com', nome: 'A', ativo: true, criadoEm: '', perfis: [] },
      isAuthenticated: true,
      isLoading: false,
    })
    renderWithRouter('/login')
    expect(screen.getByText('página inicial')).toBeInTheDocument()
    expect(screen.queryByText('página pública')).not.toBeInTheDocument()
  })

  it('renderiza Outlet quando não autenticado', () => {
    mockUseAuth.mockReturnValue({ user: null, isAuthenticated: false, isLoading: false })
    renderWithRouter('/login')
    expect(screen.getByText('página pública')).toBeInTheDocument()
    expect(screen.queryByText('página inicial')).not.toBeInTheDocument()
  })
})
