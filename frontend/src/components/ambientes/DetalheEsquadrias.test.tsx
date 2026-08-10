import { render, screen, within } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { DetalheEsquadrias } from './DetalheEsquadrias'
import type { EsquadriasResponse } from '@/types/ambientes/ambiente'
import { Bloco, MaterialEsquadria, TipoEsquadria, Unidade } from '@/types/ambientes/enums'

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
                        informacaoAdicional: 'Com veneziana',
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
                localizacao: { id: 2, bloco: Bloco.BLOCO_1, unidade: Unidade.SEDE, andar: 0 },
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
    dadosPaginacao: {
        totalElements: 2,
        totalPages: 1,
        currentPage: 0,
        pageSize: 100,
        hasNext: false,
        hasPrevious: false,
    },
}

describe('DetalheEsquadrias', () => {
    it('renderiza o título da página', () => {
        render(<DetalheEsquadrias response={mockResponse} />)
        expect(screen.getByText('Detalhes de Esquadrias')).toBeInTheDocument()
    })

    it('renderiza nome e localização de cada ambiente', () => {
        render(<DetalheEsquadrias response={mockResponse} />)
        expect(screen.getByText('Sala 101')).toBeInTheDocument()
        expect(screen.getByText('Sala 102')).toBeInTheDocument()
        expect(screen.getByText(/Sede • Bloco 1 • 1º Andar/)).toBeInTheDocument()
        expect(screen.getByText(/Sede • Bloco 1 • Térreo/)).toBeInTheDocument()
    })

    it('renderiza tabela de esquadrias por ambiente com colunas e valores', () => {
        render(<DetalheEsquadrias response={mockResponse} />)
        const tabela = screen.getByRole('table', { name: 'Esquadrias de Sala 101' })
        expect(within(tabela).getByText('Janela')).toBeInTheDocument()
        expect(within(tabela).getByText('Porta')).toBeInTheDocument()
        expect(within(tabela).getByText('Alumínio')).toBeInTheDocument()
        expect(within(tabela).getByText('Madeira Maciça')).toBeInTheDocument()
        expect(within(tabela).getByText('1.50')).toBeInTheDocument()
        expect(within(tabela).getByText('1.20')).toBeInTheDocument()
        // 0.90 aparece duas vezes: peitoril da Janela e largura (base) da Porta
        expect(within(tabela).getAllByText('0.90')).toHaveLength(2)
        expect(within(tabela).getByText('3.60')).toBeInTheDocument()
        expect(within(tabela).getByText('Com veneziana')).toBeInTheDocument()
    })

    it('exibe peitoril formatado quando altura > 0', () => {
        render(<DetalheEsquadrias response={mockResponse} />)
        const tabela = screen.getByRole('table', { name: 'Esquadrias de Sala 101' })
        // Peitoril 0.90 (Janela) e largura 0.90 (Porta) — ambos presentes
        expect(within(tabela).getAllByText('0.90')).toHaveLength(2)
    })

    it('exibe travessão quando peitoril é 0', () => {
        render(<DetalheEsquadrias response={mockResponse} />)
        const tabela = screen.getByRole('table', { name: 'Esquadrias de Sala 102' })
        const linhas = within(tabela).getAllByRole('row')
        // Linha 0 = header, linha 1 = esquadria
        const celulas = within(linhas[1]).getAllByRole('cell')
        // Coluna peitoril (índice 4)
        expect(celulas[4]).toHaveTextContent('—')
    })

    it('exibe travessão quando informação adicional está vazia', () => {
        render(<DetalheEsquadrias response={mockResponse} />)
        const tabela = screen.getByRole('table', { name: 'Esquadrias de Sala 102' })
        const linhas = within(tabela).getAllByRole('row')
        const celulas = within(linhas[1]).getAllByRole('cell')
        // Coluna informação adicional (índice 7)
        expect(celulas[7]).toHaveTextContent('—')
    })

    it('renderiza resumo por tipo/material de cada ambiente', () => {
        render(<DetalheEsquadrias response={mockResponse} />)
        // Sala 101 tem 2 itens no resumo, Sala 102 tem 1
        const resumos = screen.getAllByText('Resumo:')
        expect(resumos).toHaveLength(2)
        // "Janela / Alumínio: 3.60 m²" aparece no resumo da Sala 101 e no global
        expect(screen.getAllByText(/Janela \/ Alumínio: 3.60 m²/)).toHaveLength(2)
        // "Porta / Madeira Maciça: 1.89 m²" aparece no resumo da Sala 101 e no global
        expect(screen.getAllByText(/Porta \/ Madeira Maciça: 1.89 m²/)).toHaveLength(2)
        // "Janela / Vidro: 1.00 m²" aparece no resumo da Sala 102 e no global
        expect(screen.getAllByText(/Janela \/ Vidro: 1.00 m²/)).toHaveLength(2)
    })

    it('renderiza resumo global com totalTipoMaterial', () => {
        render(<DetalheEsquadrias response={mockResponse} />)
        expect(screen.getByText('Resumo Global')).toBeInTheDocument()
        // Cada item do global também aparece no resumo de algum ambiente
        expect(screen.getAllByText(/Janela \/ Alumínio: 3.60 m²/)).toHaveLength(2)
        expect(screen.getAllByText(/Porta \/ Madeira Maciça: 1.89 m²/)).toHaveLength(2)
    })

    it('usa keys estáveis baseadas em id para esquadrias', () => {
        render(<DetalheEsquadrias response={mockResponse} />)
        const tabela = screen.getByRole('table', { name: 'Esquadrias de Sala 101' })
        const linhas = within(tabela).getAllByRole('row')
        // header + 2 esquadrias
        expect(linhas).toHaveLength(3)
    })

    it('exibe mensagem quando ambiente não tem esquadrias para os filtros', () => {
        const responseSemEsquadrias: EsquadriasResponse = {
            ...mockResponse,
            ambientes: [
                {
                    ...mockResponse.ambientes[0],
                    detalhesEsquadrias: {
                        esquadrias: [],
                        esquadriasTipoMaterial: [],
                    },
                },
            ],
        }
        render(<DetalheEsquadrias response={responseSemEsquadrias} />)
        expect(
            screen.getByText('Nenhuma esquadria para os filtros aplicados.'),
        ).toBeInTheDocument()
    })

    it('não renderiza resumo por ambiente quando vazio', () => {
        const responseSemResumo: EsquadriasResponse = {
            ...mockResponse,
            ambientes: [
                {
                    ...mockResponse.ambientes[0],
                    detalhesEsquadrias: {
                        esquadrias: mockResponse.ambientes[0].detalhesEsquadrias.esquadrias,
                        esquadriasTipoMaterial: [],
                    },
                },
            ],
        }
        render(<DetalheEsquadrias response={responseSemResumo} />)
        expect(screen.queryByText('Resumo:')).not.toBeInTheDocument()
    })

    it('não renderiza resumo global quando vazio', () => {
        const responseSemGlobal: EsquadriasResponse = {
            ...mockResponse,
            totalTipoMaterial: [],
        }
        render(<DetalheEsquadrias response={responseSemGlobal} />)
        expect(screen.queryByText('Resumo Global')).not.toBeInTheDocument()
    })
})