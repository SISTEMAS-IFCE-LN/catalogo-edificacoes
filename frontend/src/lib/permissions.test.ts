import {describe, it, expect} from 'vitest'
import {hasPermission, matchRoute} from './permissions'
import {Role} from '@/types/user'

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