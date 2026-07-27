import { describe, it, expect, beforeEach } from 'vitest'
import { setAccessToken, getAccessToken, clearAccessToken } from './auth'

beforeEach(() => {
    clearAccessToken()
})

describe('access token em memória', () => {
    it('set/get/clear funcionam', () => {
        expect(getAccessToken()).toBeNull()
        setAccessToken('abc')
        expect(getAccessToken()).toBe('abc')
        clearAccessToken()
        expect(getAccessToken()).toBeNull()
    })

    it('setAccessToken aceita null', () => {
        setAccessToken('abc')
        expect(getAccessToken()).toBe('abc')
        setAccessToken(null)
        expect(getAccessToken()).toBeNull()
    })
})
