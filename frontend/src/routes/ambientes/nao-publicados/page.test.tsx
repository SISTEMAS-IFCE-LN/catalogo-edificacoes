import {fireEvent, render, screen, waitFor, within} from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {MemoryRouter, Route, Routes} from 'react-router'
import {QueryClient, QueryClientProvider} from '@tanstack/react-query'
import {toast} from 'sonner'
import {NaoPublicadosPage} from './page'
import type {AmbientesBasicosPaginados} from '@/types/ambientes/response'
import type {User} from '@/types/usuarios/user'
import {Role} from '@/types/usuarios/user'
import {Bloco, TipoAmbiente, Unidade} from '@/types/ambientes/enums'

vi.mock('@/lib/api/api-naopublicados', () => ({
    fetchNaoPublicados: vi.fn(),
    deletarAmbientes: vi.fn(),
    enviarParaValidacao: vi.fn(),
}))

vi.mock('sonner', () => ({
    toast: {success: vi.fn(), error: vi.fn()},
}))

vi.mock('@/hooks/useAuth', () => ({
    useAuth: vi.fn(),
}))

import {deletarAmbientes, enviarParaValidacao, fetchNaoPublicados} from '@/lib/api/api-naopublicados'
import {useAuth} from '@/hooks/useAuth'

const userGestor: User = {
    id: 1,
    email: 'gestor@ifce.edu.br',
    nome: 'Gestor Teste',
    ativo: true,
    criadoEm: '2025-01-01T00:00:00.000Z',
    perfis: [Role.GESTOR_SISTEMA],
}

const PAGINA: AmbientesBasicosPaginados = {
    ambientes: [
        {
            id: 1,
            nome: 'Sala 101',
            tipo: TipoAmbiente.SALA_AULA,
            localizacao: {id: 3, bloco: Bloco.BLOCO_1, unidade: Unidade.SEDE, andar: 2},
            capacidade: 30,
            area: 50,
        },
        {
            id: 2,
            nome: 'Laboratório 1',
            tipo: TipoAmbiente.LABORATORIO,
            localizacao: {id: 4, bloco: Bloco.BLOCO_2, unidade: Unidade.SEDE, andar: 1},
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
            queries: {retry: false, gcTime: 0, staleTime: 0, refetchOnWindowFocus: false},
        },
    })
}

function renderPage(client?: QueryClient) {
    return render(
        <QueryClientProvider client={client ?? createQueryClient()}>
            <MemoryRouter initialEntries={['/ambientes/nao-publicados']}>
                <Routes>
                    <Route path="/ambientes/nao-publicados" element={<NaoPublicadosPage/>}/>
                    <Route path="/ambientes/nao-publicados/:id" element={<div>detalhe-ambiente</div>}/>
                </Routes>
            </MemoryRouter>
        </QueryClientProvider>,
    )
}

// Seleciona o item, escolhe a ação no AcoesLote e clica em Executar — abre o
// ModalConfirmacao correspondente.
async function selecionarEExecutarAcao(nomeAcao: string, nomeItem = 'Sala 101') {
    const user = userEvent.setup()
    await user.click(await screen.findByLabelText(`Selecionar ${nomeItem}`))
    await user.click(screen.getByLabelText('Selecionar ação em lote'))
    await user.click(await screen.findByRole('option', {name: nomeAcao}))
    await user.click(screen.getByRole('button', {name: 'Executar'}))
}

describe('NaoPublicadosPage', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        vi.mocked(useAuth).mockReturnValue({
            user: userGestor,
            isAuthenticated: true,
            isLoading: false,
            login: vi.fn(),
            logout: vi.fn(),
            refreshUser: vi.fn(),
        })
    })

    it('busca os não publicados e renderiza a tabela a partir do mock', async () => {
        vi.mocked(fetchNaoPublicados).mockResolvedValue(PAGINA)
        renderPage()

        expect(await screen.findByText('Sala 101')).toBeInTheDocument()
        expect(screen.getByText('Laboratório 1')).toBeInTheDocument()
        expect(fetchNaoPublicados).toHaveBeenCalled()
    })

    it('o link do nome navega para o detalhe via detalheBasePath', async () => {
        vi.mocked(fetchNaoPublicados).mockResolvedValue(PAGINA)
        const user = userEvent.setup()
        renderPage()

        const link = await screen.findByRole('link', {name: 'Sala 101'})
        expect(link).toHaveAttribute('href', '/ambientes/nao-publicados/1')

        await user.click(link)
        expect(await screen.findByText('detalhe-ambiente')).toBeInTheDocument()
    })

    it('ao selecionar um item, o AcoesLote expõe as 2 ações do gestor', async () => {
        vi.mocked(fetchNaoPublicados).mockResolvedValue(PAGINA)
        const user = userEvent.setup()
        renderPage()

        await user.click(await screen.findByLabelText('Selecionar Sala 101'))

        expect(await screen.findByRole('region', {name: 'Ações em lote'})).toBeInTheDocument()
        await user.click(screen.getByLabelText('Selecionar ação em lote'))
        expect(await screen.findByRole('option', {name: 'Enviar p/ Validação'})).toBeInTheDocument()
        expect(screen.getByRole('option', {name: 'Deletar'})).toBeInTheDocument()
    })

    it('no sucesso do lote chama a API, mostra toast e invalida a query', async () => {
        vi.mocked(fetchNaoPublicados).mockResolvedValue(PAGINA)
        vi.mocked(enviarParaValidacao).mockResolvedValueOnce(undefined)
        const queryClient = createQueryClient()
        const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries')
        renderPage(queryClient)

        await selecionarEExecutarAcao('Enviar p/ Validação')
        expect(await screen.findByText('Enviar para validação?')).toBeInTheDocument()
        fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', {name: 'Confirmar'}))

        await waitFor(() => expect(enviarParaValidacao).toHaveBeenCalledWith([1]))
        expect(toast.success).toHaveBeenCalledWith('Operação concluída.')
        expect(invalidateSpy).toHaveBeenCalledWith({queryKey: ['ambientes', 'nao-publicados']})
    })

    it('deletar o lote chama deletarAmbientes com os ids selecionados', async () => {
        vi.mocked(fetchNaoPublicados).mockResolvedValue(PAGINA)
        vi.mocked(deletarAmbientes).mockResolvedValueOnce(undefined)
        renderPage()

        await selecionarEExecutarAcao('Deletar')
        expect(await screen.findByText('Deletar ambientes selecionados?')).toBeInTheDocument()
        fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', {name: 'Confirmar'}))

        await waitFor(() => expect(deletarAmbientes).toHaveBeenCalledWith([1]))
        expect(toast.success).toHaveBeenCalledWith('Operação concluída.')
    })

    it('no erro do lote mostra a mensagem do backend e mantém o modal aberto', async () => {
        vi.mocked(fetchNaoPublicados).mockResolvedValue(PAGINA)
        vi.mocked(enviarParaValidacao).mockRejectedValueOnce(
            Object.assign(new Error('bad request'), {
                isAxiosError: true,
                response: {
                    status: 400,
                    data: {mensagem: 'Ambiente já está aguardando validação.'},
                },
            }),
        )
        renderPage()

        await selecionarEExecutarAcao('Enviar p/ Validação')
        fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', {name: 'Confirmar'}))

        await waitFor(() => {
            expect(toast.error).toHaveBeenCalledWith('Ambiente já está aguardando validação.')
        })
        // confirmarLote não tem try/catch: a promise rejeita e o modal segue aberto
        expect(screen.getByText('Enviar para validação?')).toBeInTheDocument()
        expect(toast.success).not.toHaveBeenCalled()
    })
})
