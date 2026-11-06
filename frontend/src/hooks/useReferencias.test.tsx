import {type ReactNode} from 'react'
import {describe, it, expect, beforeEach, afterEach} from 'vitest'
import {renderHook, waitFor} from '@testing-library/react'
import {QueryClient, QueryClientProvider} from '@tanstack/react-query'
import MockAdapter from 'axios-mock-adapter'
import {api} from '@/lib/api/api'
import {useBlocos, useCampus, useUnidades} from './useReferencias'

let mockApi: MockAdapter
let queryClient: QueryClient

function wrapper({children}: {children: ReactNode}) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
}

// Aguarda um ciclo para confirmar que uma query desabilitada não disparou HTTP.
async function aguardarCiclo() {
    await new Promise((resolve) => setTimeout(resolve, 50))
}

beforeEach(() => {
    mockApi = new MockAdapter(api, {onNoMatch: 'throwException'})
    queryClient = new QueryClient({
        defaultOptions: {
            queries: {retry: false, gcTime: Infinity},
        },
    })
})

afterEach(() => {
    mockApi.restore()
    queryClient.clear()
})

describe('useCampus', () => {
    it('busca uma vez e mantém o resultado em cache', async () => {
        mockApi.onGet('/api/campus').reply(200, [{id: 1, nome: 'LIMOEIRO DO NORTE'}])

        const {result, rerender} = renderHook(() => useCampus(), {wrapper})

        await waitFor(() => expect(result.current.isSuccess).toBe(true))
        expect(result.current.data).toEqual([{id: 1, nome: 'LIMOEIRO DO NORTE'}])

        rerender()
        await waitFor(() => expect(result.current.isSuccess).toBe(true))

        expect(mockApi.history.get).toHaveLength(1)
    })
})

describe('useUnidades', () => {
    it('não dispara HTTP quando o campus é null', async () => {
        const {result} = renderHook(() => useUnidades(null), {wrapper})

        await aguardarCiclo()

        expect(result.current.fetchStatus).toBe('idle')
        expect(mockApi.history.get).toHaveLength(0)
    })

    it('dispara GET /api/unidades?campusId=1 quando o campus existe', async () => {
        mockApi.onGet('/api/unidades').reply(200, [{id: 10, nome: 'SEDE'}])

        const {result} = renderHook(() => useUnidades(1), {wrapper})

        await waitFor(() => expect(result.current.isSuccess).toBe(true))

        expect(mockApi.history.get).toHaveLength(1)
        expect(mockApi.history.get[0].url).toBe('/api/unidades')
        expect(mockApi.history.get[0].params).toEqual({campusId: 1})
        expect(result.current.data).toEqual([{id: 10, nome: 'SEDE'}])
    })

    it('faz nova query quando o campusId muda', async () => {
        mockApi.onGet('/api/unidades').reply((config) => {
            const campusId = config.params?.campusId
            return [
                200,
                campusId === 1
                    ? [{id: 10, nome: 'SEDE'}]
                    : [{id: 20, nome: 'CIDADE ALTA'}],
            ]
        })

        const {result, rerender} = renderHook(
            ({campusId}: {campusId: number}) => useUnidades(campusId),
            {wrapper, initialProps: {campusId: 1}},
        )

        await waitFor(() => expect(result.current.data).toEqual([{id: 10, nome: 'SEDE'}]))

        rerender({campusId: 2})

        await waitFor(() => expect(result.current.data).toEqual([{id: 20, nome: 'CIDADE ALTA'}]))

        expect(mockApi.history.get).toHaveLength(2)
        expect(mockApi.history.get[1].params).toEqual({campusId: 2})
    })
})

describe('useBlocos', () => {
    it('não dispara HTTP quando a unidade é null', async () => {
        const {result} = renderHook(() => useBlocos(null), {wrapper})

        await aguardarCiclo()

        expect(result.current.fetchStatus).toBe('idle')
        expect(mockApi.history.get).toHaveLength(0)
    })

    it('dispara GET /api/blocos?unidadeId=2 quando a unidade existe', async () => {
        mockApi.onGet('/api/blocos').reply(200, [{id: 30, nome: 'BLOCO 1'}])

        const {result} = renderHook(() => useBlocos(2), {wrapper})

        await waitFor(() => expect(result.current.isSuccess).toBe(true))

        expect(mockApi.history.get).toHaveLength(1)
        expect(mockApi.history.get[0].url).toBe('/api/blocos')
        expect(mockApi.history.get[0].params).toEqual({unidadeId: 2})
        expect(result.current.data).toEqual([{id: 30, nome: 'BLOCO 1'}])
    })
})
