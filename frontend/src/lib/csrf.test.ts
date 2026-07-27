import { describe, it, expect, beforeEach } from 'vitest'
import { getCsrfToken } from './csrf'

beforeEach(() => {
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT'
})

describe('getCsrfToken', () => {
    it('lê cookie XSRF-TOKEN quando presente', () => {
        document.cookie = 'XSRF-TOKEN=abc123'
        expect(getCsrfToken()).toBe('abc123')
    })

    it('retorna null quando cookie ausente', () => {
        expect(getCsrfToken()).toBeNull()
    })

    it('decodifica URL-encoded', () => {
        document.cookie = 'XSRF-TOKEN=hello%20world'
        expect(getCsrfToken()).toBe('hello world')
    })
})
