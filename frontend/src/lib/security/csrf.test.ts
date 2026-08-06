import { describe, it, expect, afterEach, vi } from 'vitest'
import axios from 'axios'
import { getCsrfToken, ensureCsrfToken, clearCsrfToken } from './csrf'

afterEach(() => {
    clearCsrfToken()
    vi.restoreAllMocks()
})

describe('getCsrfToken', () => {
    it('retorna null antes de ensureCsrfToken', () => {
        expect(getCsrfToken()).toBeNull()
    })
})

describe('ensureCsrfToken', () => {
    it('faz GET /auth/csrf-token e armazena token mascarado do body', async () => {
        const spy = vi.spyOn(axios, 'get').mockResolvedValueOnce({
            data: { token: 'mascarado-abc' },
        } as Awaited<ReturnType<typeof axios.get>>)

        await ensureCsrfToken()

        expect(spy).toHaveBeenCalledWith(
            expect.stringContaining('/auth/csrf-token'),
            { withCredentials: true },
        )
        expect(getCsrfToken()).toBe('mascarado-abc')
    })

    it('NÃO refaz GET se token já está em memória (idempotente)', async () => {
        const spy = vi.spyOn(axios, 'get').mockResolvedValueOnce({
            data: { token: 'mascarado-1' },
        } as Awaited<ReturnType<typeof axios.get>>)

        await ensureCsrfToken()
        await ensureCsrfToken()

        expect(spy).toHaveBeenCalledTimes(1)
        expect(getCsrfToken()).toBe('mascarado-1')
    })
})

describe('clearCsrfToken', () => {
    it('limpa o token em memória', async () => {
        vi.spyOn(axios, 'get').mockResolvedValueOnce({
            data: { token: 'mascarado-x' },
        } as Awaited<ReturnType<typeof axios.get>>)

        await ensureCsrfToken()
        expect(getCsrfToken()).toBe('mascarado-x')

        clearCsrfToken()
        expect(getCsrfToken()).toBeNull()
    })
})
