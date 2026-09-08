import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter, useLocation } from 'react-router'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { HeaderNav } from './HeaderNav'
import { Role } from '@/types/usuarios/user'

const mockUseAuth = vi.fn()

vi.mock('@/hooks/useAuth', () => ({
  useAuth: () => mockUseAuth(),
}))

vi.mock('react-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router')>()
  return {
    ...actual,
    Link: ({ to, className, children, ...props }: { to: string } & React.AnchorHTMLAttributes<HTMLAnchorElement>) => (
      <a href={to} className={className} data-testid={`link-${to}`} {...props}>
        {children}
      </a>
    ),
    useLocation: vi.fn(),
  }
})

function renderComponent() {
  return render(
    <MemoryRouter>
      <HeaderNav />
    </MemoryRouter>,
  )
}

describe('HeaderNav — anônimo', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseAuth.mockReturnValue({ user: null })
  })

  it('não renderiza nada se usuário não autenticado', () => {
    renderComponent()
    expect(screen.queryByRole('button', { name: /Abrir menu de navegação/i })).not.toBeInTheDocument()
  })
})

describe('HeaderNav — autenticado', () => {
  const mockLocation = { pathname: '/ambientes/publicados' }

  beforeEach(() => {
    vi.clearAllMocks()
    ;(useLocation as ReturnType<typeof vi.fn>).mockReturnValue(mockLocation)
  })

  it('renderiza o botão hamburger', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        nome: 'Teste',
        email: 'test@ifce.edu.br',
        perfis: [Role.COLABORADOR],
        ativo: true,
        criadoEm: '',
      },
    })
    renderComponent()
    expect(screen.getByRole('button', { name: /Abrir menu de navegação/i })).toBeInTheDocument()
  })

  it('renderiza grupo "Ambientes" quando usuário tem ROLE_COLABORADOR', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        nome: 'Teste',
        email: 'test@ifce.edu.br',
        perfis: [Role.COLABORADOR],
        ativo: true,
        criadoEm: '',
      },
    })
    renderComponent()

    const trigger = screen.getByRole('button', { name: /Abrir menu de navegação/i })
    fireEvent.click(trigger)

    expect(screen.getByText('Ambientes')).toBeInTheDocument()
    expect(screen.getByText('Listar')).toBeInTheDocument()
    expect(screen.queryByText('Validar')).not.toBeInTheDocument()
    expect(screen.queryByText('Gerir')).not.toBeInTheDocument()
  })

  it('renderiza grupo "Usuários" quando usuário tem ROLE_ADMINISTRADOR', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        nome: 'Admin',
        email: 'admin@ifce.edu.br',
        perfis: [Role.ADMINISTRADOR, Role.COLABORADOR],
        ativo: true,
        criadoEm: '',
      },
    })
    renderComponent()

    const trigger = screen.getByRole('button', { name: /Abrir menu de navegação/i })
    fireEvent.click(trigger)

    expect(screen.getByText('Usuários')).toBeInTheDocument()
    expect(screen.getByText('Gerir')).toBeInTheDocument()
  })

  it('não renderiza grupo "Usuários" se usuário não tem ROLE_ADMINISTRADOR', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        nome: 'Validador',
        email: 'validador@ifce.edu.br',
        perfis: [Role.VALIDADOR, Role.COLABORADOR],
        ativo: true,
        criadoEm: '',
      },
    })
    renderComponent()

    const trigger = screen.getByRole('button', { name: /Abrir menu de navegação/i })
    fireEvent.click(trigger)

    expect(screen.queryByText('Usuários')).not.toBeInTheDocument()
  })

  it('destaca o link ativo conforme pathname', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        nome: 'Teste',
        email: 'test@ifce.edu.br',
        perfis: [Role.COLABORADOR],
        ativo: true,
        criadoEm: '',
      },
    })
    renderComponent()

    const trigger = screen.getByRole('button', { name: /Abrir menu de navegação/i })
    fireEvent.click(trigger)

    const listLink = screen.getByTestId('link-/ambientes/publicados')
    expect(listLink).toHaveClass('bg-accent')
  })

  it('não destaca link inativo', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        nome: 'Teste',
        email: 'test@ifce.edu.br',
        perfis: [Role.COLABORADOR, Role.VALIDADOR],
        ativo: true,
        criadoEm: '',
      },
    })
    renderComponent()

    const trigger = screen.getByRole('button', { name: /Abrir menu de navegação/i })
    fireEvent.click(trigger)

    const validarLink = screen.getByTestId('link-/ambientes/validacao')
    expect(validarLink).not.toHaveClass('bg-accent')
  })

  it('renderiza "Gerir" do grupo Ambientes para GESTOR_SISTEMA', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        nome: 'Gestor',
        email: 'gestor@ifce.edu.br',
        perfis: [Role.GESTOR_SISTEMA, Role.COLABORADOR],
        ativo: true,
        criadoEm: '',
      },
    })
    renderComponent()

    const trigger = screen.getByRole('button', { name: /Abrir menu de navegação/i })
    fireEvent.click(trigger)

    expect(screen.getByText('Gerir')).toBeInTheDocument()
  })
})
