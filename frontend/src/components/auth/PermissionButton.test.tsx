import {render, screen} from '@testing-library/react'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {PermissionButton} from './PermissionButton'
import {Role} from '@/types/user'

const mockUseAuth = vi.fn()

vi.mock('@/hooks/useAuth', () => ({
    useAuth: () => mockUseAuth(),
}))

describe('PermissionButton', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('renderiza o botão quando usuário tem a permissão', () => {
        mockUseAuth.mockReturnValue({
            user: {
                id: 1, email: 'a@b.com', nome: 'A', ativo: true, criadoEm: '',
                perfis: [Role.COLABORADOR, Role.VALIDADOR],
            },
            isAuthenticated: true,
            isLoading: false,
        })
        render(
            <PermissionButton requiredRoles={[Role.VALIDADOR]}>
                Publicar
            </PermissionButton>,
        )
        expect(screen.getByRole('button', {name: /publicar/i})).toBeInTheDocument()
    })

    it('não renderiza quando usuário não tem a permissão', () => {
        mockUseAuth.mockReturnValue({
            user: {
                id: 1, email: 'a@b.com', nome: 'A', ativo: true, criadoEm: '',
                perfis: [Role.COLABORADOR],
            },
            isAuthenticated: true,
            isLoading: false,
        })
        const {container} = render(
            <PermissionButton requiredRoles={[Role.VALIDADOR]}>
                Publicar
            </PermissionButton>,
        )
        expect(container.innerHTML).toBe('')
    })

    it('não renderiza quando não há usuário', () => {
        mockUseAuth.mockReturnValue({user: null, isAuthenticated: false, isLoading: false})
        const {container} = render(
            <PermissionButton requiredRoles={[Role.VALIDADOR]}>
                Publicar
            </PermissionButton>,
        )
        expect(container.innerHTML).toBe('')
    })

    it('repassa props para o Button', () => {
        mockUseAuth.mockReturnValue({
            user: {
                id: 1, email: 'a@b.com', nome: 'A', ativo: true, criadoEm: '',
                perfis: [Role.COLABORADOR, Role.VALIDADOR],
            },
            isAuthenticated: true,
            isLoading: false,
        })
        const handleClick = vi.fn()
        render(
            <PermissionButton requiredRoles={[Role.VALIDADOR]} onClick={handleClick}>
                Publicar
            </PermissionButton>,
        )
        const button = screen.getByRole('button', {name: /publicar/i})
        expect(button).toBeInTheDocument()
        button.click()
        expect(handleClick).toHaveBeenCalledTimes(1)
    })

    it('renderiza children corretamente', () => {
        mockUseAuth.mockReturnValue({
            user: {
                id: 1, email: 'a@b.com', nome: 'A', ativo: true, criadoEm: '',
                perfis: [Role.COLABORADOR, Role.ADMINISTRADOR],
            },
            isAuthenticated: true,
            isLoading: false,
        })
        render(
            <PermissionButton requiredRoles={[Role.ADMINISTRADOR]}>
                <span>Editar Perfis</span>
            </PermissionButton>,
        )
        expect(screen.getByText(/editar perfis/i)).toBeInTheDocument()
    })
})
