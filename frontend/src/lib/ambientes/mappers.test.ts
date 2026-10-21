import {describe, it, expect} from 'vitest'
import {ambienteDeDetalhe, dadosBasicosDeDetalhe, duplicacaoDeDetalhe, esquadriasDeDetalhe, geometriasDeDetalhe} from './mappers'
import type {AmbienteDetalhe} from '@/types/ambientes/response'
import {MaterialEsquadria, StatusAmbiente, TipoAmbiente, TipoEsquadria, TipoGeometria} from '@/types/ambientes/enums'

const CAMPUS = {id: 1, nome: 'LIMOEIRO DO NORTE'}
const UNIDADE = {id: 1, nome: 'SEDE'}
const BLOCO = {id: 1, nome: 'BLOCO 1'}

// Payload de localização esperado: IDs existentes e `nome: null` (o backend
// reusa a entidade; enviar nome junto seria "id + nome" e daria 400).
const LOCALIZACAO_ESPERADA = {
    campus: {id: 1, nome: null},
    unidade: {id: 1, nome: null},
    bloco: {id: 1, nome: null},
    andar: 2,
}

const AMBIENTE: AmbienteDetalhe = {
    id: 7,
    nome: 'Sala 101',
    tipo: TipoAmbiente.SALA_AULA,
    localizacao: {
        id: 3,
        campus: CAMPUS,
        unidade: UNIDADE,
        bloco: BLOCO,
        andar: 2,
    },
    capacidade: 30,
    geometrias: [
        {id: 10, tipo: TipoGeometria.RETANGULAR, base: 4, altura: 3, repeticao: 2, area: 24},
    ],
    areaAmbiente: 24,
    pesDireitos: [3],
    esquadriasDetalhes: {
        esquadrias: [
            {
                id: 11,
                tipo: TipoEsquadria.PORTA,
                geometria: {id: 12, base: 0.9, altura: 2.1, repeticao: 1, area: 1.89},
                alturaPeitoril: 0,
                area: 1.89,
                material: MaterialEsquadria.ALUMINIO,
                informacaoAdicional: 'Porta dupla',
            },
            {
                id: 13,
                tipo: TipoEsquadria.JANELA,
                geometria: {id: 14, base: 1.5, altura: 1.2, repeticao: 3, area: 5.4},
                alturaPeitoril: 1.1,
                area: 5.4,
                material: MaterialEsquadria.FERRO_VIDRO,
                informacaoAdicional: '',
            },
        ],
        esquadriasTipoMaterial: [],
    },
    informacaoAdicional: 'Sala com ar-condicionado',
    status: StatusAmbiente.NAO_PUBLICADO,
}

describe('mappers (resposta → request)', () => {
    it('dadosBasicosDeDetalhe devolve os IDs da localização com nome null', () => {
        expect(dadosBasicosDeDetalhe(AMBIENTE)).toEqual({
            nome: 'Sala 101',
            capacidade: 30,
            localizacao: LOCALIZACAO_ESPERADA,
        })
    })

    it('duplicacaoDeDetalhe mantém nome/localizacao ORIGINAIS (sem sufixo "(cópia)")', () => {
        const duplicacao = duplicacaoDeDetalhe(AMBIENTE)
        expect(duplicacao).toEqual({
            nome: 'Sala 101',
            localizacao: LOCALIZACAO_ESPERADA,
        })
        expect(duplicacao.nome).not.toContain('cópia')
    })

    it('geometriasDeDetalhe converte o rótulo do tipo para a chave técnica', () => {
        expect(geometriasDeDetalhe(AMBIENTE)).toEqual([
            {tipo: 'RETANGULAR', base: 4, altura: 3, repeticao: 2},
        ])
    })

    it('esquadriasDeDetalhe converte tipo/material e mantém geometria, peitoril e info', () => {
        expect(esquadriasDeDetalhe(AMBIENTE)).toEqual([
            {
                tipo: 'PORTA',
                material: 'ALUMINIO',
                geometria: {base: 0.9, altura: 2.1, repeticao: 1},
                alturaPeitoril: 0,
                informacaoAdicional: 'Porta dupla',
            },
            {
                tipo: 'JANELA',
                material: 'FERRO_VIDRO',
                geometria: {base: 1.5, altura: 1.2, repeticao: 3},
                alturaPeitoril: 1.1,
                informacaoAdicional: '',
            },
        ])
    })

    it('ambienteDeDetalhe monta o AmbienteReq COMPLETO (UC16: POST /{id})', () => {
        expect(ambienteDeDetalhe(AMBIENTE)).toEqual({
            nome: 'Sala 101',
            tipo: 'SALA_AULA',
            capacidade: 30,
            localizacao: LOCALIZACAO_ESPERADA,
            geometrias: [{tipo: 'RETANGULAR', base: 4, altura: 3, repeticao: 2}],
            pesDireitos: [3],
            esquadrias: [
                {
                    tipo: 'PORTA',
                    material: 'ALUMINIO',
                    geometria: {base: 0.9, altura: 2.1, repeticao: 1},
                    alturaPeitoril: 0,
                    informacaoAdicional: 'Porta dupla',
                },
                {
                    tipo: 'JANELA',
                    material: 'FERRO_VIDRO',
                    geometria: {base: 1.5, altura: 1.2, repeticao: 3},
                    alturaPeitoril: 1.1,
                    informacaoAdicional: '',
                },
            ],
            informacaoAdicional: 'Sala com ar-condicionado',
        })
    })

    it('lança erro para rótulo de enum desconhecido (falha rápida, não envia payload inválido)', () => {
        const corrompido = {
            ...AMBIENTE,
            tipo: 'Tipo Inexistente',
        } as unknown as AmbienteDetalhe

        expect(() => ambienteDeDetalhe(corrompido)).toThrow('Rótulo desconhecido: Tipo Inexistente')
    })
})
