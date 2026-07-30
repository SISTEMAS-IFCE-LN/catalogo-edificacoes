import {render, screen} from '@testing-library/react'
import {MemoryRouter} from 'react-router'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {ProtectedNavigation} from './ProtectedNavigation'
import {Role} from '@/types/user'

const mockUseAuth = vi.fn()

vi.mock('@/hooks/useAuth', () => ({
    useAuth: () => mockUseAuth(),
}))

function renderWithAuth() {
    return render(
        <MemoryRouter>
            <ProtectedNavigation/>
        </MemoryRouter>,
    )
}

describe('ProtectedNavigation', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('não renderiza itens quando não há usuário', () => {
        mockUseAuth.mockReturnValue({user: null, isAuthenticated: false, isLoading: false})
        renderWithAuth()
        expect(screen.queryByText('Publicados')).not.toBeInTheDocument()
        expect(screen.queryByText('Aguardando Validação')).not.toBeInTheDocument()
        expect(screen.queryByText('Não Publicados')).not.toBeInTheDocument()
        expect(screen.queryByText('Usuários')).not.toBeInTheDocument()
    })

    it('renderiza apenas "Publicados" quando usuário tem apenas COLABORADOR', () => {
        mockUseAuth.mockReturnValue({
            user: {
                id: 1,
                email: 'a@b.com',
                nome: 'A',
                ativo: true,
                criadoEm: '',
                perfis: [Role.COLABORADOR],
            },
            isAuthenticated: true,
            isLoading: false,
        })
        renderWithAuth()
        expect(screen.getByText('Publicados')).toBeInTheDocument()
        expect(screen.queryByText('Aguardando Validação')).not.toBeInTheDocument()
        expect(screen.queryByText('Não Publicados')).not.toBeInTheDocument()
        expect(screen.queryByText('Usuários')).not.toBeInTheDocument()
    })

    it('renderiza todos os itens quando usuário tem todos os perfis (FakeAuth)', () => {
        mockUseAuth.mockReturnValue({
            user: {
                id: 1,
                email: 'dev@ifce.edu.br',
                nome: 'Dev FakeAuth',
                ativo: true,
                criadoEm: '',
                perfis: [Role.COLABORADOR, Role.VALIDADOR, Role.GESTOR_SISTEMA, Role.ADMINISTRADOR],
            },
            isAuthenticated: true,
            isLoading: false,
        })
        renderWithAuth()
        expect(screen.getByText('Publicados')).toBeInTheDocument()
        expect(screen.getByText('Aguardando Validação')).toBeInTheDocument()
        expect(screen.getByText('Não Publicados')).toBeInTheDocument()
        expect(screen.getByText('Usuários')).toBeInTheDocument()
    })

    it('renderiza itens para VALIDADOR', () => {
        mockUseAuth.mockReturnValue({
            user: {
                id: 1,
                email: 'a@b.com',
                nome: 'A',
                ativo: true,
                criadoEm: '',
                perfis: [Role.COLABORADOR, Role.VALIDADOR],
            },
            isAuthenticated: true,
            isLoading: false,
        })
        renderWithAuth()
        expect(screen.getByText('Publicados')).toBeInTheDocument()
        expect(screen.getByText('Aguardando Validação')).toBeInTheDocument()
        expect(screen.queryByText('Não Publicados')).not.toBeInTheDocument()
        expect(screen.queryByText('Usuários')).not.toBeInTheDocument()
    })

    it('renderiza itens para GESTOR_SISTEMA', () => {
        mockUseAuth.mockReturnValue({
            user: {
                id: 1,
                email: 'a@b.com',
                nome: 'A',
                ativo: true,
                criadoEm: '',
                perfis: [Role.COLABORADOR, Role.GESTOR_SISTEMA],
            },
            isAuthenticated: true,
            isLoading: false,
        })
        renderWithAuth()
        expect(screen.getByText('Publicados')).toBeInTheDocument()
        expect(screen.queryByText('Aguardando Validação')).not.toBeInTheDocument()
        expect(screen.getByText('Não Publicados')).toBeInTheDocument()
        expect(screen.queryByText('Usuários')).not.toBeInTheDocument()
    })

    it('renderiza itens para ADMINISTRADOR', () => {
        mockUseAuth.mockReturnValue({
            user: {
                id: 1,
                email: 'a@b.com',
                nome: 'A',
                ativo: true,
                criadoEm: '',
                perfis: [Role.COLABORADOR, Role.ADMINISTRADOR],
            },
            isAuthenticated: true,
            isLoading: false,
        })
        renderWithAuth()
        expect(screen.getByText('Publicados')).toBeInTheDocument()
        expect(screen.queryByText('Aguardando Validação')).not.toBeInTheDocument()
        expect(screen.queryByText('Não Publicados')).not.toBeInTheDocument()
        expect(screen.getByText('Usuários')).toBeInTheDocument()
    })
})
