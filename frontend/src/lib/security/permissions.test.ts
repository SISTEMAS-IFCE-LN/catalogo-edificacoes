import {describe, it, expect} from 'vitest'
import {hasPermission, matchRoute, getRequiredRoles} from './permissions'
import {Role} from '@/types/usuarios/user'

describe('hasPermission', () => {
    it('true quando interseção de roles não é vazia', () => {
        expect(hasPermission([Role.COLABORADOR], [Role.COLABORADOR])).toBe(true)
        expect(hasPermission([Role.COLABORADOR, Role.VALIDADOR], [Role.VALIDADOR])).toBe(true)
    })
    it('false quando sem interseção', () => {
        expect(hasPermission([Role.COLABORADOR], [Role.VALIDADOR])).toBe(false)
    })
})

describe('matchRoute', () => {
    it('match com parâmetro dinâmico', () => {
        expect(matchRoute('/ambientes/publicados/:id', '/ambientes/publicados/123')).toBe(true)
        expect(matchRoute('/ambientes/publicados/:id', '/ambientes/publicados/123/edit')).toBe(false)
    })
    it('match exato sem parâmetros', () => {
        expect(matchRoute('/usuarios', '/usuarios')).toBe(true)
        expect(matchRoute('/usuarios', '/usuarios/1')).toBe(false)
    })
})

describe('getRequiredRoles', () => {
    it('retorna roles para rota estática', () => {
        expect(getRequiredRoles('/usuarios')).toEqual([Role.ADMINISTRADOR])
    })
    it('retorna null para rota pública', () => {
        expect(getRequiredRoles('/ambientes/publicados')).toBeNull()
    })
    it('match com parâmetro dinâmico', () => {
        expect(getRequiredRoles('/ambientes/publicados/123')).toEqual([Role.COLABORADOR])
    })
    it('match com rota esquadrias', () => {
        expect(getRequiredRoles('/ambientes/publicados/esquadrias')).toEqual([Role.COLABORADOR])
    })
    it('retorna roles para rota validacao', () => {
        expect(getRequiredRoles('/ambientes/validacao')).toEqual([Role.VALIDADOR])
    })
    it('retorna roles para rota nao-publicados', () => {
        expect(getRequiredRoles('/ambientes/nao-publicados')).toEqual([Role.GESTOR_SISTEMA])
    })
})