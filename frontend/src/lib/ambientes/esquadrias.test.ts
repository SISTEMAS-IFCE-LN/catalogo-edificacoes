import { describe, it, expect } from 'vitest'
import {
    FILTRO_ESQUADRIAS_VAZIO,
    filtrarEsquadrias,
    filtroEsquadriasVazio,
    obterIdsInvalidos,
    resumirPorTipoMaterial,
    temEsquadriasVisiveis,
} from './esquadrias'
import type { EsquadriasResponse } from '@/types/ambientes/ambiente'
import { Bloco, MaterialEsquadria, TipoEsquadria, Unidade } from '@/types/ambientes/enums'

const dadosPaginacao = {
    totalElements: 2,
    totalPages: 1,
    currentPage: 0,
    pageSize: 100,
    hasNext: false,
    hasPrevious: false,
}

const mockResponse: EsquadriasResponse = {
    ambientes: [
        {
            dadosAmbiente: {
                id: 1,
                nome: 'Sala 101',
                localizacao: { id: 1, bloco: Bloco.BLOCO_1, unidade: Unidade.SEDE, andar: 1 },
            },
            detalhesEsquadrias: {
                esquadrias: [
                    {
                        id: 10,
                        tipo: TipoEsquadria.JANELA,
                        geometria: { id: 100, base: 1.5, altura: 1.2, repeticao: 2, area: 3.6 },
                        alturaPeitoril: 0.9,
                        area: 3.6,
                        material: MaterialEsquadria.ALUMINIO,
                        informacaoAdicional: '',
                    },
                    {
                        id: 11,
                        tipo: TipoEsquadria.PORTA,
                        geometria: { id: 101, base: 0.9, altura: 2.1, repeticao: 1, area: 1.89 },
                        alturaPeitoril: 0,
                        area: 1.89,
                        material: MaterialEsquadria.MADEIRA_MACICA,
                        informacaoAdicional: '',
                    },
                ],
                esquadriasTipoMaterial: [
                    { tipo: TipoEsquadria.JANELA, material: MaterialEsquadria.ALUMINIO, area: 3.6 },
                    { tipo: TipoEsquadria.PORTA, material: MaterialEsquadria.MADEIRA_MACICA, area: 1.89 },
                ],
            },
        },
        {
            dadosAmbiente: {
                id: 2,
                nome: 'Sala 102',
                localizacao: { id: 2, bloco: Bloco.BLOCO_1, unidade: Unidade.SEDE, andar: 1 },
            },
            detalhesEsquadrias: {
                esquadrias: [
                    {
                        id: 20,
                        tipo: TipoEsquadria.JANELA,
                        geometria: { id: 200, base: 1.0, altura: 1.0, repeticao: 1, area: 1.0 },
                        alturaPeitoril: 0,
                        area: 1.0,
                        material: MaterialEsquadria.VIDRO,
                        informacaoAdicional: '',
                    },
                ],
                esquadriasTipoMaterial: [
                    { tipo: TipoEsquadria.JANELA, material: MaterialEsquadria.VIDRO, area: 1.0 },
                ],
            },
        },
    ],
    totalTipoMaterial: [
        { tipo: TipoEsquadria.JANELA, material: MaterialEsquadria.ALUMINIO, area: 3.6 },
        { tipo: TipoEsquadria.PORTA, material: MaterialEsquadria.MADEIRA_MACICA, area: 1.89 },
        { tipo: TipoEsquadria.JANELA, material: MaterialEsquadria.VIDRO, area: 1.0 },
    ],
    dadosPaginacao,
}

describe('filtroEsquadriasVazio', () => {
    it('retorna true quando filtro está vazio', () => {
        expect(filtroEsquadriasVazio(FILTRO_ESQUADRIAS_VAZIO)).toBe(true)
        expect(filtroEsquadriasVazio({ tipo: '', material: '' })).toBe(true)
    })

    it('retorna false quando tipo está preenchido', () => {
        expect(filtroEsquadriasVazio({ tipo: 'Janela', material: '' })).toBe(false)
    })

    it('retorna false quando material está preenchido', () => {
        expect(filtroEsquadriasVazio({ tipo: '', material: 'Alumínio' })).toBe(false)
    })
})

describe('filtrarEsquadrias', () => {
    it('retorna a resposta original quando filtro está vazio', () => {
        const result = filtrarEsquadrias(mockResponse, FILTRO_ESQUADRIAS_VAZIO)
        expect(result).toBe(mockResponse)
    })

    it('filtra por tipo mantendo apenas esquadrias do tipo selecionado', () => {
        const result = filtrarEsquadrias(mockResponse, { tipo: TipoEsquadria.PORTA })
        const esquadrias = result.ambientes[0].detalhesEsquadrias.esquadrias
        expect(esquadrias).toHaveLength(1)
        expect(esquadrias[0].tipo).toBe(TipoEsquadria.PORTA)
    })

    it('filtra por material mantendo apenas esquadrias do material selecionado', () => {
        const result = filtrarEsquadrias(mockResponse, { material: MaterialEsquadria.ALUMINIO })
        const esquadrias = result.ambientes[0].detalhesEsquadrias.esquadrias
        expect(esquadrias).toHaveLength(1)
        expect(esquadrias[0].material).toBe(MaterialEsquadria.ALUMINIO)
    })

    it('filtra por tipo e material simultaneamente', () => {
        const result = filtrarEsquadrias(mockResponse, {
            tipo: TipoEsquadria.JANELA,
            material: MaterialEsquadria.VIDRO,
        })
        const esquadriasAmb0 = result.ambientes[0].detalhesEsquadrias.esquadrias
        const esquadriasAmb1 = result.ambientes[1].detalhesEsquadrias.esquadrias
        expect(esquadriasAmb0).toHaveLength(0)
        expect(esquadriasAmb1).toHaveLength(1)
        expect(esquadriasAmb1[0].material).toBe(MaterialEsquadria.VIDRO)
    })

    it('recalcula o resumo por tipo/material de cada ambiente após filtro', () => {
        const result = filtrarEsquadrias(mockResponse, { tipo: TipoEsquadria.PORTA })
        const resumo = result.ambientes[0].detalhesEsquadrias.esquadriasTipoMaterial
        expect(resumo).toEqual([
            { tipo: TipoEsquadria.PORTA, material: MaterialEsquadria.MADEIRA_MACICA, area: 1.89 },
        ])
    })

    it('mantém o resumo global totalTipoMaterial intacto', () => {
        const result = filtrarEsquadrias(mockResponse, { tipo: TipoEsquadria.PORTA })
        expect(result.totalTipoMaterial).toBe(mockResponse.totalTipoMaterial)
    })

    it('mantém dadosPaginacao intacto', () => {
        const result = filtrarEsquadrias(mockResponse, { tipo: TipoEsquadria.PORTA })
        expect(result.dadosPaginacao).toEqual(dadosPaginacao)
    })

    it('mantém dadosAmbiente intacto em cada ambiente', () => {
        const result = filtrarEsquadrias(mockResponse, { tipo: TipoEsquadria.PORTA })
        expect(result.ambientes[0].dadosAmbiente).toEqual(mockResponse.ambientes[0].dadosAmbiente)
    })
})

describe('resumirPorTipoMaterial', () => {
    it('retorna array vazio quando não há esquadrias', () => {
        expect(resumirPorTipoMaterial([])).toEqual([])
    })

    it('agrupa áreas por (tipo, material)', () => {
        const esquadrias = [
            { ...mockResponse.ambientes[0].detalhesEsquadrias.esquadrias[0], area: 3.6 },
            { ...mockResponse.ambientes[0].detalhesEsquadrias.esquadrias[0], id: 99, area: 2.0 },
        ]
        const resumo = resumirPorTipoMaterial(esquadrias)
        expect(resumo).toHaveLength(1)
        expect(resumo[0].area).toBe(5.6)
    })

    it('cria entradas separadas para combinações distintas', () => {
        const esquadrias = mockResponse.ambientes[0].detalhesEsquadrias.esquadrias
        const resumo = resumirPorTipoMaterial(esquadrias)
        expect(resumo).toHaveLength(2)
    })
})

describe('obterIdsInvalidos', () => {
    it('retorna array vazio quando todos os IDs foram retornados', () => {
        expect(obterIdsInvalidos([1, 2], mockResponse)).toEqual([])
    })

    it('retorna IDs solicitados mas ausentes na resposta', () => {
        expect(obterIdsInvalidos([1, 2, 3, 4], mockResponse)).toEqual([3, 4])
    })

    it('retorna todos os IDs quando a resposta não tem ambientes', () => {
        const vazia: EsquadriasResponse = {
            ambientes: [],
            totalTipoMaterial: [],
            dadosPaginacao,
        }
        expect(obterIdsInvalidos([1, 2, 3], vazia)).toEqual([1, 2, 3])
    })

    it('retorna array vazio quando não há IDs solicitados', () => {
        expect(obterIdsInvalidos([], mockResponse)).toEqual([])
    })

    it('preserva a ordem dos IDs inválidos conforme solicitados', () => {
        expect(obterIdsInvalidos([5, 1, 6, 2, 7], mockResponse)).toEqual([5, 6, 7])
    })
})

describe('temEsquadriasVisiveis', () => {
    it('retorna true quando há ao menos uma esquadria em algum ambiente', () => {
        expect(temEsquadriasVisiveis(mockResponse.ambientes)).toBe(true)
    })

    it('retorna false quando todos os ambientes não têm esquadrias', () => {
        const ambientes = [
            {
                ...mockResponse.ambientes[0],
                detalhesEsquadrias: { esquadrias: [], esquadriasTipoMaterial: [] },
            },
        ]
        expect(temEsquadriasVisiveis(ambientes)).toBe(false)
    })

    it('retorna false quando a lista de ambientes está vazia', () => {
        expect(temEsquadriasVisiveis([])).toBe(false)
    })
})