import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter } from 'react-router'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { PublicadosPage } from './page'
import type { AmbientesBasicosPaginados } from '@/types/ambientes/ambiente'
import { Bloco, TipoAmbiente, TipoFiltro, Unidade } from '@/types/ambientes/enums'
import type { User } from '@/types/usuarios/user'
import { Role } from '@/types/usuarios/user'

vi.mock('@/lib/api/api-ambientes', () => ({
    fetchAmbientes: vi.fn(),
}))

vi.mock('sonner', () => ({
    toast: { error: vi.fn() },
}))

vi.mock('@/hooks/useAuth', () => ({
    useAuth: vi.fn(),
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

import { fetchAmbientes } from '@/lib/api/api-ambientes'
import { useAuth } from '@/hooks/useAuth'

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

const userAutenticado: User = {
    id: 1,
    email: 'user@ifce.edu.br',
    nome: 'Usuário Teste',
    ativo: true,
    criadoEm: '2025-01-01T00:00:00.000Z',
    perfis: [Role.COLABORADOR],
}

function createQueryClient() {
    return new QueryClient({
        defaultOptions: {
            queries: { retry: false, gcTime: 0, staleTime: 0, refetchOnWindowFocus: false },
        },
    })
}

function renderPage(initialEntries: string[] = ['/ambientes/publicados']) {
    return render(
        <QueryClientProvider client={createQueryClient()}>
            <MemoryRouter initialEntries={initialEntries}>
                <PublicadosPage />
            </MemoryRouter>
        </QueryClientProvider>,
    )
}

describe('PublicadosPage', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        mockNavigate.mockClear()
    })

    describe('usuário anônimo (UC21-FE)', () => {
        it('não renderiza checkbox nem AcoesLote', async () => {
            vi.mocked(useAuth).mockReturnValue({
                user: null,
                isAuthenticated: false,
                isLoading: false,
                login: vi.fn(),
                logout: vi.fn(),
                refreshUser: vi.fn(),
            })
            vi.mocked(fetchAmbientes).mockResolvedValueOnce(mockData)
            renderPage()
            await waitFor(() => {
                expect(screen.getByText('Sala 101')).toBeInTheDocument()
            })
            expect(screen.queryByLabelText('Selecionar todos da página')).not.toBeInTheDocument()
            expect(screen.queryByLabelText('Selecionar Sala 101')).not.toBeInTheDocument()
            expect(screen.queryByRole('region', { name: 'Ações em lote' })).not.toBeInTheDocument()
            expect(screen.getByText('Área Total: 90.00 m²')).toBeInTheDocument()
        })
    })

    describe('usuário autenticado (UC20-FE)', () => {
        function mockAuthAutenticado() {
            vi.mocked(useAuth).mockReturnValue({
                user: userAutenticado,
                isAuthenticated: true,
                isLoading: false,
                login: vi.fn(),
                logout: vi.fn(),
                refreshUser: vi.fn(),
            })
        }

        it('renderiza checkbox e AcoesLote', async () => {
            mockAuthAutenticado()
            vi.mocked(fetchAmbientes).mockResolvedValueOnce(mockData)
            renderPage()
            await waitFor(() => {
                expect(screen.getByLabelText('Selecionar todos da página')).toBeInTheDocument()
                expect(screen.getByLabelText('Selecionar Sala 101')).toBeInTheDocument()
            })
            // AcoesLote não aparece sem seleção
            expect(screen.queryByRole('region', { name: 'Ações em lote' })).not.toBeInTheDocument()
            expect(screen.getByText('Área Total: 90.00 m²')).toBeInTheDocument()
        })

        it('selecionar um item exibe AcoesLote com contador', async () => {
            mockAuthAutenticado()
            vi.mocked(fetchAmbientes).mockResolvedValueOnce(mockData)
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
            mockAuthAutenticado()
            vi.mocked(fetchAmbientes).mockResolvedValueOnce(mockData)
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
            mockAuthAutenticado()
            vi.mocked(fetchAmbientes).mockResolvedValueOnce(mockData)
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

        it('navega para esquadrias ao executar ação Detalhes Esquadrias', async () => {
            const user = userEvent.setup()
            mockAuthAutenticado()
            vi.mocked(fetchAmbientes).mockResolvedValueOnce(mockData)
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
            // useNavigate deve ser chamado com a rota de esquadrias e ids
            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith(
                    '/ambientes/publicados/esquadrias?ids=1,2',
                )
            })
        })

        it('aplica filtro por tipo enviando o nome do enum', async () => {
            const user = userEvent.setup()
            mockAuthAutenticado()
            vi.mocked(fetchAmbientes).mockResolvedValue(mockData)
            renderPage()
            await waitFor(() => {
                expect(screen.getByLabelText('Selecionar todos da página')).toBeInTheDocument()
            })
            // Selecionar tipo de filtro "Tipo"
            await user.click(screen.getByLabelText('Tipo de filtro'))
            await user.click(await screen.findByRole('option', { name: 'Tipo' }))
            // Selecionar "Sala de Aula" no select de tipo
            await user.click(screen.getByLabelText('Filtrar por tipo'))
            await user.click(await screen.findByRole('option', { name: 'Sala de Aula' }))
            // Aplicar
            await user.click(screen.getByText('Aplicar'))
            // fetchAmbientes deve ser chamado com tipoFiltro=TIPO e tipo=SALA_AULA
            await waitFor(() => {
                expect(fetchAmbientes).toHaveBeenCalledWith(
                    expect.objectContaining({ tipoFiltro: TipoFiltro.TIPO, tipo: 'SALA_AULA' }),
                    expect.anything(),
                )
            })
        })
    })
})