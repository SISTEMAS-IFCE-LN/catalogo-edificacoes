import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import axios from 'axios'
import MockAdapter from 'axios-mock-adapter'
import { api, refreshAccessToken } from './api'
import { getAccessToken, setAccessToken, clearAccessToken } from './auth'
import { ensureCsrfToken, clearCsrfToken } from './csrf'

let mockApi: MockAdapter
let mockAxios: MockAdapter

beforeEach(() => {
    mockApi = new MockAdapter(api, { onNoMatch: 'passthrough' })
    mockAxios = new MockAdapter(axios, { onNoMatch: 'passthrough' })
    clearAccessToken()
    clearCsrfToken()
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
        mockAxios.onGet(/\/auth\/csrf-token/).reply(200, { token: 'abc123' })
        await ensureCsrfToken()

        let captured: string | undefined
        mockApi.onPost('/auth/logout').reply((cfg) => {
            captured = cfg.headers?.['X-XSRF-TOKEN']
            return [200, {}]
        })
        await api.post('/auth/logout')
        expect(captured).toBe('abc123')
    })

    it('NÃO anexa X-XSRF-TOKEN em GET /api/*', async () => {
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

        mockAxios.onGet(/\/auth\/csrf-token/).reply(200, { token: 'test-csrf-token' })

        mockApi
            .onGet('/api/ambientes')
            .replyOnce(401, {})
            .onGet('/api/ambientes')
            .reply(200, { ok: 1 })

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
        mockAxios.onGet(/\/auth\/csrf-token/).reply(200, { token: 'any' })
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

describe('refreshAccessToken - lock síncrono', () => {
    it('chamadas concorrentes resultam em UM único POST /auth/refresh', async () => {
        mockAxios.onGet(/\/auth\/csrf-token/).reply(200, { token: 'test-csrf-token' })

        let postCount = 0
        vi.spyOn(axios, 'post').mockImplementation(async () => {
            postCount++
            await new Promise((resolve) => setTimeout(resolve, 50))
            return { data: { accessToken: 'new-jwt' } } as Awaited<
                ReturnType<typeof axios.post>
            >
        })

        const [a, b, c] = await Promise.all([
            refreshAccessToken(),
            refreshAccessToken(),
            refreshAccessToken(),
        ])

        expect(a).toBe('new-jwt')
        expect(b).toBe('new-jwt')
        expect(c).toBe('new-jwt')
        expect(postCount).toBe(1)
    })

    it('após falha, libera o lock para nova tentativa', async () => {
        mockAxios.onGet(/\/auth\/csrf-token/).reply(200, { token: 'test-csrf-token' })

        let callCount = 0
        vi.spyOn(axios, 'post').mockImplementation(async () => {
            callCount++
            if (callCount === 1) {
                const error = new Error('Request failed with status code 500') as Error & {
                    response?: { status: number; data: unknown }
                }
                error.response = { status: 500, data: { error: 'refresh failed' } }
                throw error
            }
            return { data: { accessToken: 'new-jwt' } } as Awaited<
                ReturnType<typeof axios.post>
            >
        })

        await expect(refreshAccessToken()).rejects.toThrow()
        const second = await refreshAccessToken()

        expect(second).toBe('new-jwt')
        expect(callCount).toBe(2)
    })
})
