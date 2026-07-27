import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import axios from 'axios'
import MockAdapter from 'axios-mock-adapter'
import { api } from './api'
import { getAccessToken, setAccessToken, clearAccessToken } from './auth'

let mockApi: MockAdapter
let mockAxios: MockAdapter

beforeEach(() => {
    mockApi = new MockAdapter(api, { onNoMatch: 'passthrough' })
    mockAxios = new MockAdapter(axios, { onNoMatch: 'passthrough' })
    clearAccessToken()
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT'
})

afterEach(() => {
    mockApi.restore()
    mockAxios.restore()
    vi.restoreAllMocks()
})

describe('interceptor de request', () => {
    it('injeta Authorization quando access token está em memória', async () => {
        setAccessToken('meu-jwt')
        let captured: string | undefined
        mockApi.onGet('/foo').reply((cfg) => {
            captured = cfg.headers?.Authorization
            return [200, {}]
        })
        await api.get('/foo')
        expect(captured).toBe('Bearer meu-jwt')
    })

    it('NÃO injeta Authorization se não há token', async () => {
        let captured: string | undefined
        mockApi.onGet('/foo').reply((cfg) => {
            captured = cfg.headers?.Authorization
            return [200, {}]
        })
        await api.get('/foo')
        expect(captured).toBeUndefined()
    })

    it('anexa X-XSRF-TOKEN em POST /auth/*', async () => {
        document.cookie = 'XSRF-TOKEN=abc123'
        let captured: string | undefined
        mockApi.onPost('/auth/logout').reply((cfg) => {
            captured = cfg.headers?.['X-XSRF-TOKEN']
            return [200, {}]
        })
        await api.post('/auth/logout')
        expect(captured).toBe('abc123')
    })

    it('NÃO anexa X-XSRF-TOKEN em GET /api/*', async () => {
        document.cookie = 'XSRF-TOKEN=abc123'
        let captured: string | undefined
        mockApi.onGet('/api/ambientes').reply((cfg) => {
            captured = cfg.headers?.['X-XSRF-TOKEN']
            return [200, []]
        })
        await api.get('/api/ambientes')
        expect(captured).toBeUndefined()
    })
})

describe('interceptor de response - refresh em 401', () => {
    it('em 401 (sem ser /auth/*), chama refresh e refaz original', async () => {
        setAccessToken('expired-jwt')
        document.cookie = 'XSRF-TOKEN=test-csrf-token'

        // Mock do GET /auth/csrf-token (chamado por ensureCsrfToken dentro de refreshAccessToken)
        mockAxios.onGet('/auth/csrf-token').reply(200)

        // 1ª chamada: 401. 2ª chamada (após refresh): 200.
        mockApi
            .onGet('/api/ambientes')
            .replyOnce(401, {})
            .onGet('/api/ambientes')
            .reply(200, { ok: 1 })

        // refreshAccessToken usa axios.post (global), não api.post
        const refreshSpy = vi.spyOn(axios, 'post').mockResolvedValueOnce({
            data: { accessToken: 'new-jwt' },
        } as Awaited<ReturnType<typeof axios.post>>)

        const { data } = await api.get('/api/ambientes')

        expect(getAccessToken()).toBe('new-jwt')
        expect(refreshSpy).toHaveBeenCalledTimes(1)
        expect(refreshSpy).toHaveBeenCalledWith(
            expect.stringContaining('/auth/refresh'),
            {},
            expect.objectContaining({
                withCredentials: true,
                headers: expect.objectContaining({ 'X-XSRF-TOKEN': 'test-csrf-token' }),
            }),
        )
        expect(data).toEqual({ ok: 1 })
    })

    it('em 401 vindo de /auth/*, NÃO tenta refresh', async () => {
        const refreshSpy = vi.spyOn(axios, 'post')
        mockApi.onPost('/auth/logout').reply(401)

        await expect(api.post('/auth/logout')).rejects.toMatchObject({
            response: { status: 401 },
        })
        expect(refreshSpy).not.toHaveBeenCalled()
    })

    it('em falha de refresh, limpa token e despacha auth:logout', async () => {
        setAccessToken('expired-jwt')
        mockApi.onGet('/api/ambientes').reply(401, {})
        mockAxios.onGet('/auth/csrf-token').reply(200)
        mockAxios.onPost('/auth/refresh').reply(500)

        const handler = vi.fn()
        window.addEventListener('auth:logout', handler)

        await expect(api.get('/api/ambientes')).rejects.toMatchObject({
            response: { status: 401 },
        })
        expect(getAccessToken()).toBeNull()
        expect(handler).toHaveBeenCalledTimes(1)

        window.removeEventListener('auth:logout', handler)
    })
})