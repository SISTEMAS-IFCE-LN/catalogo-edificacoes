import {describe, it, expect, beforeEach, afterEach, vi} from 'vitest'
import MockAdapter from 'axios-mock-adapter'
import {api} from '@/lib/api/api'
import {
    alterarTipo,
    atualizarDadosBasicos,
    atualizarInfoAdicional,
    criarAmbiente,
    deletarAmbientes,
    duplicarAmbiente,
    enviarParaValidacao,
    incluirEsquadrias,
    incluirGeometrias,
} from './api-naopublicados'
import type {AmbienteInput, EsquadriaInput, GeometriaInput} from '@/types/ambientes/request'

const ROTA = '/api/ambientes/nao-publicados'

// Payload válido de exemplo aceito por AmbienteDetalheSchema.parse (rótulos no
// DTO de resposta + nomes técnicos normalizados pelo schema).
const AMBIENTE_DETALHE = {
    id: 1,
    nome: 'Sala 101',
    tipo: 'Sala de Aula',
    localizacao: {id: 3, bloco: 'Bloco 1', unidade: 'Sede', andar: 2},
    capacidade: 30,
    geometrias: [{id: 10, tipo: 'RETANGULAR', base: 4, altura: 3, repeticao: 2, area: 24}],
    areaAmbiente: 24,
    pesDireitos: [3],
    esquadriasDetalhes: {esquadrias: [], esquadriasTipoMaterial: []},
    informacaoAdicional: '',
    status: 'AGUARDANDO_VALIDACAO',
}

const AMBIENTE_INPUT: AmbienteInput = {
    nome: 'Sala 01',
    localizacao: {bloco: 'BLOCO_1', unidade: 'SEDE', andar: 0},
    tipo: 'SALA_AULA',
    capacidade: 1,
    geometrias: [{tipo: 'RETANGULAR', base: 4, altura: 3, repeticao: 1}],
    pesDireitos: [2.8],
    esquadrias: [
        {
            tipo: 'PORTA',
            geometria: {base: 0.9, altura: 2.1, repeticao: 1},
            material: 'ALUMINIO',
            alturaPeitoril: 0,
            informacaoAdicional: '',
        },
    ],
    informacaoAdicional: 'Sala com ar-condicionado',
}

let mockApi: MockAdapter

// Corpo enviado pelo axios é serializado em JSON (string). Este helper garante
// o pré-requisito e devolve o objeto para as asserções sobre nomes técnicos.
function corpoJson(data: unknown): Record<string, unknown> {
    if (typeof data !== 'string') {
        throw new Error(`Esperava corpo serializado como string, recebeu ${typeof data}`)
    }
    return JSON.parse(data) as Record<string, unknown>
}

beforeEach(() => {
    mockApi = new MockAdapter(api)
})

afterEach(() => {
    mockApi.restore()
    vi.restoreAllMocks()
})

describe('atualizarInfoAdicional', () => {
    it('envia a string CRUA como corpo com Content-Type text/plain (nunca objeto)', async () => {
        let capturado: {corpo: unknown; contentType: unknown} = {corpo: undefined, contentType: undefined}
        mockApi.onPatch(`${ROTA}/7/informacao-adicional`).reply((config) => {
            capturado = {corpo: config.data, contentType: config.headers?.['Content-Type']}
            return [200, {}]
        })

        await atualizarInfoAdicional(7, 'Sala com ar-condicionado')

        expect(capturado.corpo).toBe('Sala com ar-condicionado')
        expect(typeof capturado.corpo).toBe('string')
        expect(String(capturado.contentType)).toContain('text/plain')
    })
})

describe('contrato de enums (nomes técnicos, nunca rótulos)', () => {
    it('criarAmbiente faz POST na rota base com AmbienteReq técnico', async () => {
        let capturado: unknown
        mockApi.onPost(ROTA).reply((config) => {
            capturado = config.data
            return [201, AMBIENTE_DETALHE]
        })

        await criarAmbiente(AMBIENTE_INPUT)

        const corpo = corpoJson(capturado)
        expect(corpo.tipo).toBe('SALA_AULA')
        expect(corpo.localizacao).toEqual({bloco: 'BLOCO_1', unidade: 'SEDE', andar: 0})
        const geometrias = corpo.geometrias as Array<{tipo: string}>
        expect(geometrias[0].tipo).toBe('RETANGULAR')
        const esquadrias = corpo.esquadrias as Array<{tipo: string; material: string}>
        expect(esquadrias[0].tipo).toBe('PORTA')
        expect(esquadrias[0].material).toBe('ALUMINIO')
        expect(corpo.tipo).not.toBe('Sala de Aula')
    })

    it('alterarTipo faz POST /{id} com o AmbienteReq completo', async () => {
        let capturado: unknown
        mockApi.onPost(`${ROTA}/7`).reply((config) => {
            capturado = config.data
            return [201, AMBIENTE_DETALHE]
        })

        await alterarTipo(7, AMBIENTE_INPUT)

        const corpo = corpoJson(capturado)
        expect(corpo.tipo).toBe('SALA_AULA')
        expect((corpo.localizacao as {bloco: string}).bloco).toBe('BLOCO_1')
    })

    it('atualizarDadosBasicos faz PATCH /{id}/dados-basicos com localizacao técnica', async () => {
        let capturado: unknown
        mockApi.onPatch(`${ROTA}/7/dados-basicos`).reply((config) => {
            capturado = config.data
            return [200, {}]
        })

        await atualizarDadosBasicos(7, {
            nome: 'Sala 102',
            localizacao: {bloco: 'BLOCO_1', unidade: 'SEDE', andar: 2},
            capacidade: 30,
        })

        expect(corpoJson(capturado)).toEqual({
            nome: 'Sala 102',
            localizacao: {bloco: 'BLOCO_1', unidade: 'SEDE', andar: 2},
            capacidade: 30,
        })
    })

    it('incluirGeometrias faz PATCH /{id}/geometrias/incluir com tipo técnico', async () => {
        const geometrias: GeometriaInput[] = [{tipo: 'RETANGULAR', base: 4, altura: 3, repeticao: 1}]
        let capturado: unknown
        mockApi.onPatch(`${ROTA}/7/geometrias/incluir`).reply((config) => {
            capturado = config.data
            return [200, {}]
        })

        await incluirGeometrias(7, geometrias)

        const corpo = corpoJson(capturado)
        expect(corpo).toEqual([{tipo: 'RETANGULAR', base: 4, altura: 3, repeticao: 1}])
    })

    it('incluirEsquadrias faz PATCH /{id}/esquadrias/incluir com tipo/material técnicos', async () => {
        const esquadrias: EsquadriaInput[] = [
            {
                tipo: 'PORTA',
                geometria: {base: 0.9, altura: 2.1, repeticao: 1},
                material: 'ALUMINIO',
                alturaPeitoril: 0,
                informacaoAdicional: '',
            },
        ]
        let capturado: unknown
        mockApi.onPatch(`${ROTA}/7/esquadrias/incluir`).reply((config) => {
            capturado = config.data
            return [200, {}]
        })

        await incluirEsquadrias(7, esquadrias)

        const corpo = corpoJson(capturado)
        expect(corpo).toEqual([
            {
                tipo: 'PORTA',
                geometria: {base: 0.9, altura: 2.1, repeticao: 1},
                material: 'ALUMINIO',
                alturaPeitoril: 0,
                informacaoAdicional: '',
            },
        ])
    })

    it('duplicarAmbiente faz POST /{id}/duplicar com nome + localizacao técnica', async () => {
        let capturado: unknown
        mockApi.onPost(`${ROTA}/7/duplicar`).reply((config) => {
            capturado = config.data
            return [201, AMBIENTE_DETALHE]
        })

        await duplicarAmbiente(7, {
            nome: 'Sala 101',
            localizacao: {bloco: 'BLOCO_1', unidade: 'SEDE', andar: 2},
        })

        const corpo = corpoJson(capturado)
        expect(corpo).toEqual({
            nome: 'Sala 101',
            localizacao: {bloco: 'BLOCO_1', unidade: 'SEDE', andar: 2},
        })
    })
})

describe('operações em lote', () => {
    it('deletarAmbientes faz DELETE na rota base enviando o array de ids', async () => {
        let capturado: unknown
        mockApi.onDelete(ROTA).reply((config) => {
            capturado = config.data
            return [200, {}]
        })

        await deletarAmbientes([1, 2, 3])

        expect(corpoJson(capturado)).toEqual([1, 2, 3])
    })

    it('enviarParaValidacao faz PATCH /validar enviando o array de ids', async () => {
        let capturado: unknown
        mockApi.onPatch(`${ROTA}/validar`).reply((config) => {
            capturado = config.data
            return [200, {}]
        })

        await enviarParaValidacao([1, 2])

        expect(corpoJson(capturado)).toEqual([1, 2])
    })
})

describe('validação das respostas 201 com AmbienteDetalheSchema', () => {
    it.each([
        ['criarAmbiente', () => criarAmbiente(AMBIENTE_INPUT)],
        ['alterarTipo', () => alterarTipo(7, AMBIENTE_INPUT)],
        ['duplicarAmbiente', () => duplicarAmbiente(7, {nome: 'Sala 101', localizacao: {bloco: 'BLOCO_1', unidade: 'SEDE', andar: 2}})],
    ])('%s devolve o AmbienteDetalhe normalizado (rótulos padronizados)', async (_nome, chamada) => {
        mockApi.onAny().reply(201, AMBIENTE_DETALHE)

        const resultado = await chamada()

        expect(resultado).toMatchObject({id: 1, areaAmbiente: 24})
        expect(resultado.geometrias[0].tipo).toBe('Retangular')
        expect(resultado.status).toBe('Aguardando Validação')
    })

    it('lança erro Zod quando a resposta 201 é inválida', async () => {
        mockApi.onPost(ROTA).reply(201, {...AMBIENTE_DETALHE, id: 'inválido'})

        await expect(criarAmbiente(AMBIENTE_INPUT)).rejects.toThrow()
    })
})
