import {describe, it, expect, beforeEach, afterEach} from 'vitest'
import MockAdapter from 'axios-mock-adapter'
import {api} from '@/lib/api/api'
import {fetchBlocos, fetchCampus, fetchUnidades} from './api-referencia'

let mockApi: MockAdapter

beforeEach(() => {
    mockApi = new MockAdapter(api, {onNoMatch: 'throwException'})
})

afterEach(() => {
    mockApi.restore()
})

describe('fetchCampus', () => {
    it('chama GET /api/campus sem params', async () => {
        mockApi.onGet('/api/campus').reply(200, [{id: 1, nome: 'LIMOEIRO DO NORTE'}])

        const resultado = await fetchCampus()

        expect(mockApi.history.get).toHaveLength(1)
        expect(mockApi.history.get[0].url).toBe('/api/campus')
        expect(mockApi.history.get[0].params).toBeUndefined()
        expect(resultado).toEqual([{id: 1, nome: 'LIMOEIRO DO NORTE'}])
    })

    it('retorna lista vazia quando a resposta é []', async () => {
        mockApi.onGet('/api/campus').reply(200, [])

        await expect(fetchCampus()).resolves.toEqual([])
    })

    it('repassa o signal ao axios', async () => {
        const controller = new AbortController()
        let sinalRecebido: unknown
        mockApi.onGet('/api/campus').reply((config) => {
            sinalRecebido = config.signal
            return [200, []]
        })

        await fetchCampus(controller.signal)

        expect(sinalRecebido).toBe(controller.signal)
    })
})

describe('fetchUnidades', () => {
    it('chama GET /api/unidades com params campusId', async () => {
        mockApi.onGet('/api/unidades').reply(200, [{id: 10, nome: 'SEDE'}])

        const resultado = await fetchUnidades(1)

        expect(mockApi.history.get).toHaveLength(1)
        expect(mockApi.history.get[0].url).toBe('/api/unidades')
        expect(mockApi.history.get[0].params).toEqual({campusId: 1})
        expect(resultado).toEqual([{id: 10, nome: 'SEDE'}])
    })

    it('repassa o signal ao axios', async () => {
        const controller = new AbortController()
        let sinalRecebido: unknown
        mockApi.onGet('/api/unidades').reply((config) => {
            sinalRecebido = config.signal
            return [200, []]
        })

        await fetchUnidades(1, controller.signal)

        expect(sinalRecebido).toBe(controller.signal)
    })
})

describe('fetchBlocos', () => {
    it('chama GET /api/blocos com params unidadeId', async () => {
        mockApi.onGet('/api/blocos').reply(200, [{id: 20, nome: 'BLOCO 1'}])

        const resultado = await fetchBlocos(2)

        expect(mockApi.history.get).toHaveLength(1)
        expect(mockApi.history.get[0].url).toBe('/api/blocos')
        expect(mockApi.history.get[0].params).toEqual({unidadeId: 2})
        expect(resultado).toEqual([{id: 20, nome: 'BLOCO 1'}])
    })

    it('repassa o signal ao axios', async () => {
        const controller = new AbortController()
        let sinalRecebido: unknown
        mockApi.onGet('/api/blocos').reply((config) => {
            sinalRecebido = config.signal
            return [200, []]
        })

        await fetchBlocos(2, controller.signal)

        expect(sinalRecebido).toBe(controller.signal)
    })
})
