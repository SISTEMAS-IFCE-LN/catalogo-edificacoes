import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter } from 'react-router'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ValidacaoPage } from './page'
import type { AmbientesBasicosPaginados } from '@/types/ambientes/ambiente'
import { Bloco, TipoAmbiente, TipoFiltro, Unidade } from '@/types/ambientes/enums'
import { toast } from 'sonner'

vi.mock('@/lib/api/api-validacao', () => ({
    fetchValidacao: vi.fn(),
}))

vi.mock('sonner', () => ({
    toast: { error: vi.fn() },
}))

vi.mock('@/hooks/useIsMobile', () => ({
    useIsMobile: () => false,
}))

const mockNavigate = vi.fn()
vi.mock('react-router', async (importOriginal) => {
    const actual = await importOriginal<typeof import('react-router')>()
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    }
})

import { fetchValidacao } from '@/lib/api/api-validacao'

const mockData: AmbientesBasicosPaginados = {
    ambientes: [
        {
            id: 1,
            nome: 'Sala 101',
            tipo: TipoAmbiente.SALA_AULA,
            localizacao: { id: 1, bloco: Bloco.BLOCO_1, unidade: Unidade.SEDE, andar: 1 },
            capacidade: 30,
            area: 50,
        },
        {
            id: 2,
            nome: 'Sala 102',
            tipo: TipoAmbiente.LABORATORIO,
            localizacao: { id: 2, bloco: Bloco.BLOCO_2, unidade: Unidade.SEDE, andar: 0 },
            capacidade: 20,
            area: 40,
        },
    ],
    areaTotal: 90,
    dadosPaginacao: {
        totalElements: 2,
        totalPages: 1,
        currentPage: 0,
        pageSize: 20,
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

function renderPage(initialEntries: string[] = ['/ambientes/validacao']) {
    return render(
        <QueryClientProvider client={createQueryClient()}>
            <MemoryRouter initialEntries={initialEntries}>
                <ValidacaoPage />
            </MemoryRouter>
        </QueryClientProvider>,
    )
}

describe('ValidacaoPage (UC01-FE)', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        mockNavigate.mockClear()
    })

    it('renderiza a lista com link para /ambientes/validacao/{id}', async () => {
        vi.mocked(fetchValidacao).mockResolvedValueOnce(mockData)
        renderPage()
        await waitFor(() => {
            expect(screen.getByText('Sala 101')).toBeInTheDocument()
        })
        expect(screen.getByRole('link', { name: 'Sala 101' })).toHaveAttribute(
            'href',
            '/ambientes/validacao/1',
        )
    })

    it('renderiza checkboxes e Área Total; AcoesLote ausente sem seleção', async () => {
        vi.mocked(fetchValidacao).mockResolvedValueOnce(mockData)
        renderPage()
        await waitFor(() => {
            expect(screen.getByLabelText('Selecionar todos da página')).toBeInTheDocument()
            expect(screen.getByLabelText('Selecionar Sala 101')).toBeInTheDocument()
        })
        expect(screen.queryByRole('region', { name: 'Ações em lote' })).not.toBeInTheDocument()
        expect(screen.getByText('Área Total: 90.00 m²')).toBeInTheDocument()
    })

    it('selecionar um item exibe AcoesLote com contador', async () => {
        vi.mocked(fetchValidacao).mockResolvedValueOnce(mockData)
        renderPage()
        await waitFor(() => {
            expect(screen.getByLabelText('Selecionar Sala 101')).toBeInTheDocument()
        })
        fireEvent.click(screen.getByLabelText('Selecionar Sala 101'))
        await waitFor(() => {
            expect(screen.getByRole('region', { name: 'Ações em lote' })).toBeInTheDocument()
            expect(screen.getByText('1 selecionado')).toBeInTheDocument()
        })
    })

    it('selecionar todos marca todas as linhas', async () => {
        vi.mocked(fetchValidacao).mockResolvedValueOnce(mockData)
        renderPage()
        await waitFor(() => {
            expect(screen.getByLabelText('Selecionar todos da página')).toBeInTheDocument()
        })
        fireEvent.click(screen.getByLabelText('Selecionar todos da página'))
        await waitFor(() => {
            expect(screen.getByText('2 selecionados')).toBeInTheDocument()
        })
    })

    it('Limpar desmarca a seleção', async () => {
        vi.mocked(fetchValidacao).mockResolvedValueOnce(mockData)
        renderPage()
        await waitFor(() => {
            expect(screen.getByLabelText('Selecionar Sala 101')).toBeInTheDocument()
        })
        fireEvent.click(screen.getByLabelText('Selecionar Sala 101'))
        await waitFor(() => {
            expect(screen.getByText('Limpar')).toBeInTheDocument()
        })
        fireEvent.click(screen.getByText('Limpar'))
        await waitFor(() => {
            expect(screen.queryByRole('region', { name: 'Ações em lote' })).not.toBeInTheDocument()
        })
    })

    it('executar Detalhar Esquadrias navega para /ambientes/validacao/esquadrias?ids=1,2', async () => {
        const user = userEvent.setup()
        vi.mocked(fetchValidacao).mockResolvedValueOnce(mockData)
        renderPage()
        await waitFor(() => {
            expect(screen.getByLabelText('Selecionar todos da página')).toBeInTheDocument()
        })
        // Selecionar todos
        await user.click(screen.getByLabelText('Selecionar todos da página'))
        // Abrir seletor de ação
        await waitFor(() => {
            expect(screen.getByLabelText('Selecionar ação em lote')).toBeInTheDocument()
        })
        await user.click(screen.getByLabelText('Selecionar ação em lote'))
        await user.click(await screen.findByRole('option', { name: 'Detalhar Esquadrias' }))
        // Executar
        await user.click(screen.getByText('Executar'))
        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith(
                '/ambientes/validacao/esquadrias?ids=1,2',
            )
        })
    })

    it('em erro de query dispara toast.error e exibe Tentar novamente', async () => {
        vi.mocked(fetchValidacao).mockRejectedValueOnce(new Error('rede'))
        renderPage()
        await waitFor(() => {
            expect(screen.getByText('Tentar novamente')).toBeInTheDocument()
        })
        expect(toast.error).toHaveBeenCalledWith(
            'Erro ao carregar ambientes em validação. Tente novamente.',
        )
    })

    it('limpa a seleção ao mudar de página', async () => {
        const paginada: AmbientesBasicosPaginados = {
            ...mockData,
            dadosPaginacao: {
                totalElements: 30,
                totalPages: 2,
                currentPage: 0,
                pageSize: 20,
                hasNext: true,
                hasPrevious: false,
            },
        }
        vi.mocked(fetchValidacao).mockResolvedValue(paginada)
        renderPage()
        await waitFor(() => {
            expect(screen.getByLabelText('Selecionar Sala 101')).toBeInTheDocument()
        })
        fireEvent.click(screen.getByLabelText('Selecionar Sala 101'))
        await waitFor(() => {
            expect(screen.getByRole('region', { name: 'Ações em lote' })).toBeInTheDocument()
        })
        fireEvent.click(screen.getByText('Próximo'))
        await waitFor(() => {
            expect(screen.queryByRole('region', { name: 'Ações em lote' })).not.toBeInTheDocument()
        })
    })

    it('aplica filtro por tipo enviando o nome do enum', async () => {
        const user = userEvent.setup()
        vi.mocked(fetchValidacao).mockResolvedValue(mockData)
        renderPage()
        await waitFor(() => {
            expect(screen.getByLabelText('Tipo de filtro')).toBeInTheDocument()
        })
        // Selecionar um item para verificar que aplicar filtro limpa a seleção
        await waitFor(() => {
            expect(screen.getByLabelText('Selecionar Sala 101')).toBeInTheDocument()
        })
        fireEvent.click(screen.getByLabelText('Selecionar Sala 101'))
        await waitFor(() => {
            expect(screen.getByRole('region', { name: 'Ações em lote' })).toBeInTheDocument()
        })
        // Selecionar tipo de filtro "Tipo"
        await user.click(screen.getByLabelText('Tipo de filtro'))
        await user.click(await screen.findByRole('option', { name: 'Tipo' }))
        // Selecionar "Sala de Aula" no select de tipo
        await user.click(screen.getByLabelText('Filtrar por tipo'))
        await user.click(await screen.findByRole('option', { name: 'Sala de Aula' }))
        // Aplicar
        await user.click(screen.getByText('Aplicar'))
        // fetchValidacao deve ser chamado com tipoFiltro=TIPO e tipo=SALA_AULA
        await waitFor(() => {
            expect(fetchValidacao).toHaveBeenCalledWith(
                expect.objectContaining({ tipoFiltro: TipoFiltro.TIPO, tipo: 'SALA_AULA' }),
                expect.anything(),
            )
        })
        // O filtro deve limpar a seleção (AcoesLote some)
        await waitFor(() => {
            expect(screen.queryByRole('region', { name: 'Ações em lote' })).not.toBeInTheDocument()
        })
    })
})
