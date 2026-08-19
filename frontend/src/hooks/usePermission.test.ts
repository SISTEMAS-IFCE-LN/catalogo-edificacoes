import {renderHook} from '@testing-library/react'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {usePermission} from './usePermission'
import {Role} from '@/types/usuarios/user'

const mockUseAuth = vi.fn()

vi.mock('@/hooks/useAuth', () => ({
    useAuth: () => mockUseAuth(),
}))

describe('usePermission', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    describe('canDo', () => {
        it('retorna false quando não há usuário', () => {
            mockUseAuth.mockReturnValue({user: null, isAuthenticated: false, isLoading: false})
            const {result} = renderHook(() => usePermission())
            expect(result.current.canDo('ambiente:publicar')).toBe(false)
        })

        it('retorna true quando usuário tem a permissão', () => {
            mockUseAuth.mockReturnValue({
                user: {
                    id: 1, email: 'a@b.com', nome: 'A', ativo: true, criadoEm: '',
                    perfis: [Role.COLABORADOR, Role.VALIDADOR],
                },
                isAuthenticated: true,
                isLoading: false,
            })
            const {result} = renderHook(() => usePermission())
            expect(result.current.canDo('ambiente:publicar')).toBe(true)
        })

        it('retorna false quando usuário não tem a permissão', () => {
            mockUseAuth.mockReturnValue({
                user: {
                    id: 1, email: 'a@b.com', nome: 'A', ativo: true, criadoEm: '',
                    perfis: [Role.COLABORADOR],
                },
                isAuthenticated: true,
                isLoading: false,
            })
            const {result} = renderHook(() => usePermission())
            expect(result.current.canDo('ambiente:publicar')).toBe(false)
        })

        it('retorna false para ação inexistente', () => {
            mockUseAuth.mockReturnValue({
                user: {
                    id: 1, email: 'a@b.com', nome: 'A', ativo: true, criadoEm: '',
                    perfis: [Role.COLABORADOR, Role.VALIDADOR, Role.GESTOR_SISTEMA, Role.ADMINISTRADOR],
                },
                isAuthenticated: true,
                isLoading: false,
            })
            const {result} = renderHook(() => usePermission())
            expect(result.current.canDo('acao:inexistente')).toBe(false)
        })
    })

    describe('canAccess', () => {
        it('retorna true para rota pública (não listada em ROUTE_PERMISSIONS)', () => {
            mockUseAuth.mockReturnValue({
                user: {
                    id: 1, email: 'a@b.com', nome: 'A', ativo: true, criadoEm: '',
                    perfis: [Role.COLABORADOR],
                },
                isAuthenticated: true,
                isLoading: false,
            })
            const {result} = renderHook(() => usePermission())
            expect(result.current.canAccess('/ambientes/publicados')).toBe(true)
        })

        it('retorna true para rota protegida quando usuário tem permissão', () => {
            mockUseAuth.mockReturnValue({
                user: {
                    id: 1, email: 'a@b.com', nome: 'A', ativo: true, criadoEm: '',
                    perfis: [Role.COLABORADOR, Role.ADMINISTRADOR],
                },
                isAuthenticated: true,
                isLoading: false,
            })
            const {result} = renderHook(() => usePermission())
            expect(result.current.canAccess('/usuarios')).toBe(true)
        })

        it('retorna false para rota protegida quando usuário não tem permissão', () => {
            mockUseAuth.mockReturnValue({
                user: {
                    id: 1, email: 'a@b.com', nome: 'A', ativo: true, criadoEm: '',
                    perfis: [Role.COLABORADOR],
                },
                isAuthenticated: true,
                isLoading: false,
            })
            const {result} = renderHook(() => usePermission())
            expect(result.current.canAccess('/usuarios')).toBe(false)
        })

        it('retorna true para rota com parâmetro dinâmico quando usuário tem permissão', () => {
            mockUseAuth.mockReturnValue({
                user: {
                    id: 1, email: 'a@b.com', nome: 'A', ativo: true, criadoEm: '',
                    perfis: [Role.COLABORADOR],
                },
                isAuthenticated: true,
                isLoading: false,
            })
            const {result} = renderHook(() => usePermission())
            expect(result.current.canAccess('/ambientes/publicados/123')).toBe(true)
        })
    })

    describe('hasRole', () => {
        it('retorna true quando usuário tem o role', () => {
            mockUseAuth.mockReturnValue({
                user: {
                    id: 1, email: 'a@b.com', nome: 'A', ativo: true, criadoEm: '',
                    perfis: [Role.COLABORADOR, Role.VALIDADOR],
                },
                isAuthenticated: true,
                isLoading: false,
            })
            const {result} = renderHook(() => usePermission())
            expect(result.current.hasRole([Role.VALIDADOR])).toBe(true)
        })

        it('retorna false quando usuário não tem o role', () => {
            mockUseAuth.mockReturnValue({
                user: {
                    id: 1, email: 'a@b.com', nome: 'A', ativo: true, criadoEm: '',
                    perfis: [Role.COLABORADOR],
                },
                isAuthenticated: true,
                isLoading: false,
            })
            const {result} = renderHook(() => usePermission())
            expect(result.current.hasRole([Role.VALIDADOR])).toBe(false)
        })

        it('retorna false quando não há usuário', () => {
            mockUseAuth.mockReturnValue({user: null, isAuthenticated: false, isLoading: false})
            const {result} = renderHook(() => usePermission())
            expect(result.current.hasRole([Role.VALIDADOR])).toBe(false)
        })
    })
})
