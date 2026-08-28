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

    it('renderiza Área Total', async () => {
        vi.mocked(fetchValidacao).mockResolvedValueOnce(mockData)
        renderPage()
        await waitFor(() => {
            expect(screen.getByText('Área Total: 90.00 m²')).toBeInTheDocument()
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

    it('muda de página ao clicar em Próximo', async () => {
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
            expect(screen.getByText('Sala 101')).toBeInTheDocument()
        })
        fireEvent.click(screen.getByText('Próximo'))
        await waitFor(() => {
            expect(fetchValidacao).toHaveBeenLastCalledWith(
                expect.objectContaining({ page: 1 }),
                expect.anything(),
            )
        })
    })

    it('aplica filtro por tipo enviando o nome do enum', async () => {
        const user = userEvent.setup()
        vi.mocked(fetchValidacao).mockResolvedValue(mockData)
        renderPage()
        await waitFor(() => {
            expect(screen.getByLabelText('Tipo de filtro')).toBeInTheDocument()
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
    })
})
