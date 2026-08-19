import {render, screen, fireEvent} from '@testing-library/react'
import {MemoryRouter} from 'react-router'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {Header} from './Header'
import {Role} from '@/types/usuarios/user'

const mockUseAuth = vi.fn()

vi.mock('@/hooks/useAuth', () => ({
    useAuth: () => mockUseAuth(),
}))

function renderWithAuth() {
    return render(
        <MemoryRouter>
            <Header/>
        </MemoryRouter>,
    )
}

const userAutenticado = {
    id: 1,
    email: 'user@ifce.edu.br',
    nome: 'Usuário Teste',
    ativo: true,
    criadoEm: '',
    perfis: [Role.COLABORADOR],
}

describe('Header — anônimo', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        mockUseAuth.mockReturnValue({user: null, isAuthenticated: false, isLoading: false, login: vi.fn(), logout: vi.fn(), refreshUser: vi.fn()})
    })

    it('renderiza o logo/título', () => {
        renderWithAuth()
        expect(screen.getByText('Catálogo Edificações')).toBeInTheDocument()
    })

    it('não renderiza ProtectedNavigation', () => {
        renderWithAuth()
        expect(screen.queryByText('Publicados')).not.toBeInTheDocument()
    })

    it('não renderiza nome/email do usuário', () => {
        renderWithAuth()
        expect(screen.queryByText('Usuário Teste')).not.toBeInTheDocument()
        expect(screen.queryByText('user@ifce.edu.br')).not.toBeInTheDocument()
    })

    it('não renderiza botão Sair', () => {
        renderWithAuth()
        expect(screen.queryByText('Sair')).not.toBeInTheDocument()
    })

    it('renderiza botão Login como link para /login', () => {
        renderWithAuth()
        const loginLink = screen.getByText('Login')
        expect(loginLink).toBeInTheDocument()
        expect(loginLink.closest('a')).toHaveAttribute('href', '/login')
    })
})

describe('Header — autenticado', () => {
    const mockLogout = vi.fn()

    beforeEach(() => {
        vi.clearAllMocks()
        mockUseAuth.mockReturnValue({
            user: userAutenticado,
            isAuthenticated: true,
            isLoading: false,
            login: vi.fn(),
            logout: mockLogout,
            refreshUser: vi.fn(),
        })
    })

    it('renderiza o logo/título', () => {
        renderWithAuth()
        expect(screen.getByText('Catálogo Edificações')).toBeInTheDocument()
    })

    it('renderiza ProtectedNavigation', () => {
        renderWithAuth()
        expect(screen.getByText('Publicados')).toBeInTheDocument()
    })

    it('renderiza nome e email do usuário', () => {
        renderWithAuth()
        expect(screen.getByText('Usuário Teste')).toBeInTheDocument()
        expect(screen.getByText('user@ifce.edu.br')).toBeInTheDocument()
    })

    it('renderiza botão Sair', () => {
        renderWithAuth()
        expect(screen.getByText('Sair')).toBeInTheDocument()
    })

    it('não renderiza botão Login', () => {
        renderWithAuth()
        expect(screen.queryByText('Login')).not.toBeInTheDocument()
    })

    it('chama logout ao clicar em Sair', () => {
        renderWithAuth()
        fireEvent.click(screen.getByText('Sair'))
        expect(mockLogout).toHaveBeenCalledOnce()
    })
})