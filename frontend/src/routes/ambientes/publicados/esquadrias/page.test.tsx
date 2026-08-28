import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter } from 'react-router'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { EsquadriasPage } from './page'
import type { EsquadriasResponse } from '@/types/ambientes/ambiente'
import { MaterialEsquadria, TipoEsquadria } from '@/types/ambientes/enums'
import { Bloco, Unidade } from '@/types/ambientes/enums'
import { toast } from 'sonner'

vi.mock('@/lib/api/api-publicados', () => ({
    fetchEsquadriasPublicados: vi.fn(),
}))

vi.mock('sonner', () => ({
    toast: { error: vi.fn() },
}))

const mockNavigate = vi.fn()
vi.mock('react-router', async (importOriginal) => {
    const actual = await importOriginal<typeof import('react-router')>()
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    }
})

import { fetchEsquadriasPublicados } from '@/lib/api/api-publicados'

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
                ],
                esquadriasTipoMaterial: [
                    { tipo: TipoEsquadria.JANELA, material: MaterialEsquadria.ALUMINIO, area: 3.6 },
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
                        tipo: TipoEsquadria.PORTA,
                        geometria: { id: 200, base: 0.9, altura: 2.1, repeticao: 1, area: 1.89 },
                        alturaPeitoril: 0,
                        area: 1.89,
                        material: MaterialEsquadria.MADEIRA_MACICA,
                        informacaoAdicional: '',
                    },
                ],
                esquadriasTipoMaterial: [
                    { tipo: TipoEsquadria.PORTA, material: MaterialEsquadria.MADEIRA_MACICA, area: 1.89 },
                ],
            },
        },
    ],
    totalTipoMaterial: [
        { tipo: TipoEsquadria.JANELA, material: MaterialEsquadria.ALUMINIO, area: 3.6 },
        { tipo: TipoEsquadria.PORTA, material: MaterialEsquadria.MADEIRA_MACICA, area: 1.89 },
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

function createQueryClient() {
    return new QueryClient({
        defaultOptions: {
            queries: { retry: false, gcTime: 0, staleTime: 0, refetchOnWindowFocus: false },
        },
    })
}

function renderPage(
    initialEntries: string[] = ['/'],
) {
    return render(
        <QueryClientProvider client={createQueryClient()}>
            <MemoryRouter initialEntries={initialEntries}>
                <EsquadriasPage />
            </MemoryRouter>
        </QueryClientProvider>,
    )
}

describe('EsquadriasPage', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        mockNavigate.mockClear()
    })

    it('exibe "Nenhum ambiente selecionado" quando ids está vazio', () => {
        renderPage(['/ambientes/publicados/esquadrias'])
        expect(screen.getByText('Nenhum ambiente selecionado.')).toBeInTheDocument()
        expect(fetchEsquadriasPublicados).not.toHaveBeenCalled()
    })

    it('exibe loading enquanto carrega', async () => {
        vi.mocked(fetchEsquadriasPublicados).mockReturnValueOnce(new Promise(() => {}))
        renderPage(['/ambientes/publicados/esquadrias?ids=1,2'])
        expect(await screen.findByText('Carregando…')).toBeInTheDocument()
    })

    it('renderiza detalhes das esquadrias após carregar', async () => {
        vi.mocked(fetchEsquadriasPublicados).mockResolvedValueOnce(mockResponse)
        renderPage(['/ambientes/publicados/esquadrias?ids=1,2'])
        await waitFor(() => {
            expect(screen.getByText('Detalhes de Esquadrias')).toBeInTheDocument()
        })
        expect(screen.getByText('Sala 101')).toBeInTheDocument()
        expect(screen.getByText('Sala 102')).toBeInTheDocument()
    })

    it('chama fetchEsquadrias com ids e paginação', async () => {
        vi.mocked(fetchEsquadriasPublicados).mockResolvedValueOnce(mockResponse)
        renderPage(['/ambientes/publicados/esquadrias?ids=1,2,3'])
        await waitFor(() => {
            expect(fetchEsquadriasPublicados).toHaveBeenCalledWith({
                ids: [1, 2, 3],
                page: 0,
                size: 100,
            }, expect.anything())
        })
    })

    it('filtra ids inválidos (não numéricos, não inteiros, <= 0)', async () => {
        vi.mocked(fetchEsquadriasPublicados).mockResolvedValueOnce(mockResponse)
        renderPage(['/ambientes/publicados/esquadrias?ids=1,abc,0,-5,1.5,2'])
        await waitFor(() => {
            expect(fetchEsquadriasPublicados).toHaveBeenCalledWith(
                expect.objectContaining({ ids: [1, 2] }),
                expect.anything(),
            )
        })
    })

    it('exibe toast de erro quando fetch falha', async () => {
        vi.mocked(fetchEsquadriasPublicados).mockRejectedValueOnce(new Error('rede'))
        renderPage(['/ambientes/publicados/esquadrias?ids=1'])
        await waitFor(() => {
            expect(toast.error).toHaveBeenCalledWith('Erro ao carregar esquadrias.')
        })
    })

    it('exibe estado de erro com botão Tentar novamente', async () => {
        vi.mocked(fetchEsquadriasPublicados).mockRejectedValueOnce(new Error('rede'))
        renderPage(['/ambientes/publicados/esquadrias?ids=1'])
        await waitFor(() => {
            expect(screen.getByText('Erro ao carregar dados.')).toBeInTheDocument()
            expect(screen.getByText('Tentar novamente')).toBeInTheDocument()
        })
    })

    it('exibe "Nenhuma esquadria encontrada" quando resposta vem vazia', async () => {
        const vazia: EsquadriasResponse = {
            ambientes: [],
            totalTipoMaterial: [],
            dadosPaginacao: {
                totalElements: 0,
                totalPages: 0,
                currentPage: 0,
                pageSize: 100,
                hasNext: false,
                hasPrevious: false,
            },
        }
        vi.mocked(fetchEsquadriasPublicados).mockResolvedValueOnce(vazia)
        renderPage(['/ambientes/publicados/esquadrias?ids=1,2'])
        await waitFor(() => {
            expect(screen.getByText('Nenhuma esquadria encontrada.')).toBeInTheDocument()
        })
    })

    it('exibe aviso de IDs inválidos quando solicitados não retornam', async () => {
        vi.mocked(fetchEsquadriasPublicados).mockResolvedValueOnce(mockResponse)
        renderPage(['/ambientes/publicados/esquadrias?ids=1,2,99'])
        await waitFor(() => {
            expect(screen.getByText(/IDs inválidos: 99/)).toBeInTheDocument()
        })
    })

    it('remove IDs inválidos ao clicar no botão e re-busca apenas válidos', async () => {
        vi.mocked(fetchEsquadriasPublicados).mockResolvedValue(mockResponse)
        renderPage(['/ambientes/publicados/esquadrias?ids=1,2,99'])
        await waitFor(() => {
            expect(screen.getByText(/IDs inválidos: 99/)).toBeInTheDocument()
        })
        fireEvent.click(screen.getByText('Remover inválidos e tentar novamente'))
        // O aviso de IDs inválidos deve desaparecer
        await waitFor(() => {
            expect(screen.queryByText(/IDs inválidos/)).not.toBeInTheDocument()
        })
        // A busca seguinte deve usar apenas ids [1,2]
        await waitFor(() => {
            const ultimaChamada = vi.mocked(fetchEsquadriasPublicados).mock.calls.at(-1)
            expect(ultimaChamada?.[0].ids).toEqual([1, 2])
        })
    })

    it('renderiza filtros de tipo e material', async () => {
        vi.mocked(fetchEsquadriasPublicados).mockResolvedValueOnce(mockResponse)
        renderPage(['/ambientes/publicados/esquadrias?ids=1'])
        await waitFor(() => {
            expect(screen.getByLabelText('Filtrar por tipo')).toBeInTheDocument()
            expect(screen.getByLabelText('Filtrar por material')).toBeInTheDocument()
        })
    })

    it('renderiza paginação com botões Anterior/Próximo', async () => {
        const paginada: EsquadriasResponse = {
            ...mockResponse,
            dadosPaginacao: {
                totalElements: 200,
                totalPages: 2,
                currentPage: 0,
                pageSize: 100,
                hasNext: true,
                hasPrevious: false,
            },
        }
        vi.mocked(fetchEsquadriasPublicados).mockResolvedValueOnce(paginada)
        renderPage(['/ambientes/publicados/esquadrias?ids=1'])
        await waitFor(() => {
            expect(screen.getByText('Anterior')).toBeInTheDocument()
            expect(screen.getByText('Próximo')).toBeInTheDocument()
            expect(screen.getByText(/Página 1 de 2/)).toBeInTheDocument()
        })
    })

    it('exibe callout de vazio pós-filtro quando nenhuma esquadria corresponde', async () => {
        vi.mocked(fetchEsquadriasPublicados).mockResolvedValueOnce(mockResponse)
        renderPage(['/ambientes/publicados/esquadrias?ids=1,2&tipo=Cobogó'])
        await waitFor(() => {
            expect(
                screen.getByText(/Nenhuma esquadria encontrada para os filtros aplicados/),
            ).toBeInTheDocument()
        })
    })
})