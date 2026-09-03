import {fireEvent, render, screen, waitFor, within} from '@testing-library/react'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {MemoryRouter, Route, Routes} from 'react-router'
import {QueryClient, QueryClientProvider} from '@tanstack/react-query'
import {NaoPublicadoDetalhePage} from './page'
import type {AmbienteDetalhe} from '@/types/ambientes/response'
import type {User} from '@/types/usuarios/user'
import {Role} from '@/types/usuarios/user'
import {Bloco, StatusAmbiente, TipoAmbiente, Unidade} from '@/types/ambientes/enums'

vi.mock('@/lib/api/api-naopublicados', () => ({
    fetchAmbienteNaoPublicado: vi.fn(),
    deletarAmbientes: vi.fn(),
    enviarParaValidacao: vi.fn(),
    duplicarAmbiente: vi.fn(),
}))

vi.mock('sonner', () => ({
    toast: {success: vi.fn(), error: vi.fn()},
}))

vi.mock('@/hooks/useAuth', () => ({
    useAuth: vi.fn(),
}))

import {
    deletarAmbientes,
    duplicarAmbiente,
    enviarParaValidacao,
    fetchAmbienteNaoPublicado,
} from '@/lib/api/api-naopublicados'
import {useAuth} from '@/hooks/useAuth'

const userGestor: User = {
    id: 1,
    email: 'gestor@ifce.edu.br',
    nome: 'Gestor Teste',
    ativo: true,
    criadoEm: '2025-01-01T00:00:00.000Z',
    perfis: [Role.GESTOR_SISTEMA],
}

const AMBIENTE: AmbienteDetalhe = {
    id: 7,
    nome: 'Sala 101',
    tipo: TipoAmbiente.SALA_AULA,
    localizacao: {
        id: 3,
        bloco: Bloco.BLOCO_1,
        unidade: Unidade.SEDE,
        andar: 2,
    },
    capacidade: 30,
    geometrias: [],
    areaAmbiente: 50,
    pesDireitos: [3],
    esquadriasDetalhes: {esquadrias: [], esquadriasTipoMaterial: []},
    informacaoAdicional: '',
    status: StatusAmbiente.NAO_PUBLICADO,
}

function createQueryClient() {
    return new QueryClient({
        defaultOptions: {
            queries: {retry: false, gcTime: 0, staleTime: 0, refetchOnWindowFocus: false},
        },
    })
}

// A rota estática `/ambientes/nao-publicados/8` captura a navegação
// pós-duplicação (o id criado), sem remontar a própria página de detalhe.
function renderPage(initialEntries: string[] = ['/ambientes/nao-publicados/7'], client?: QueryClient) {
    return render(
        <QueryClientProvider client={client ?? createQueryClient()}>
            <MemoryRouter initialEntries={initialEntries}>
                <Routes>
                    <Route path="/ambientes/nao-publicados" element={<div>lista</div>}/>
                    <Route path="/ambientes/nao-publicados/8" element={<div>detalhe-duplicado</div>}/>
                    <Route path="/ambientes/nao-publicados/:id" element={<NaoPublicadoDetalhePage/>}/>
                </Routes>
            </MemoryRouter>
        </QueryClientProvider>,
    )
}

describe('NaoPublicadoDetalhePage', () => {
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

    it('exibe loading enquanto carrega', async () => {
        vi.mocked(fetchAmbienteNaoPublicado).mockReturnValueOnce(new Promise(() => {}))
        renderPage()
        expect(await screen.findByText('Carregando…')).toBeInTheDocument()
    })

    it('não chama a API e exibe "Ambiente não encontrado" quando o id da URL é inválido', async () => {
        renderPage(['/ambientes/nao-publicados/abc'])
        await waitFor(() => {
            expect(screen.getByText('Ambiente não encontrado.')).toBeInTheDocument()
        })
        expect(screen.getByText('Voltar à lista')).toBeInTheDocument()
        expect(fetchAmbienteNaoPublicado).not.toHaveBeenCalled()
    })

    it('exibe "Ambiente não encontrado" quando a busca falha', async () => {
        vi.mocked(fetchAmbienteNaoPublicado).mockRejectedValueOnce(new Error('rede'))
        renderPage()
        await waitFor(() => {
            expect(screen.getByText('Ambiente não encontrado.')).toBeInTheDocument()
        })
        expect(screen.getByText('Voltar à lista')).toBeInTheDocument()
    })

    it('renderiza o detalhe e os botões de ação do gestor', async () => {
        vi.mocked(fetchAmbienteNaoPublicado).mockResolvedValueOnce(AMBIENTE)
        renderPage()

        await waitFor(() => {
            expect(screen.getByText('Sala 101')).toBeInTheDocument()
        })
        expect(screen.getByRole('button', {name: 'Editar Dados Básicos'})).toBeInTheDocument()
        expect(screen.getByRole('button', {name: 'Alterar Tipo'})).toBeInTheDocument()
        expect(screen.getByRole('button', {name: 'Duplicar'})).toBeInTheDocument()
        expect(screen.getByRole('button', {name: 'Enviar p/ Validação'})).toBeInTheDocument()
        expect(screen.getByRole('button', {name: 'Deletar'})).toBeInTheDocument()
    })

    describe('deletar via ModalConfirmacao (UC15-FE)', () => {
        it('deleta o ambiente e volta para a lista', async () => {
            vi.mocked(fetchAmbienteNaoPublicado).mockResolvedValue(AMBIENTE)
            vi.mocked(deletarAmbientes).mockResolvedValueOnce(undefined)
            renderPage()

            await waitFor(() => {
                expect(screen.getByRole('button', {name: 'Deletar'})).toBeEnabled()
            })
            fireEvent.click(screen.getByRole('button', {name: 'Deletar'}))
            expect(screen.getByText('Deletar ambiente?')).toBeInTheDocument()
            fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', {name: 'Deletar'}))

            await waitFor(() => {
                expect(deletarAmbientes).toHaveBeenCalledWith([7])
            })
            const {toast} = await import('sonner')
            expect(toast.success).toHaveBeenCalledWith('Operação concluída.')
            expect(await screen.findByText('lista')).toBeInTheDocument()
        })

        it('não chama a API ao cancelar o modal', async () => {
            vi.mocked(fetchAmbienteNaoPublicado).mockResolvedValueOnce(AMBIENTE)
            renderPage()

            await waitFor(() => {
                expect(screen.getByRole('button', {name: 'Deletar'})).toBeEnabled()
            })
            fireEvent.click(screen.getByRole('button', {name: 'Deletar'}))
            fireEvent.click(screen.getByRole('button', {name: 'Cancelar'}))

            await waitFor(() => {
                expect(screen.queryByText('Deletar ambiente?')).not.toBeInTheDocument()
            })
            expect(deletarAmbientes).not.toHaveBeenCalled()
        })
    })

    describe('enviar para validação via ModalConfirmacao (UC18-FE)', () => {
        it('envia o ambiente para validação e volta para a lista', async () => {
            vi.mocked(fetchAmbienteNaoPublicado).mockResolvedValue(AMBIENTE)
            vi.mocked(enviarParaValidacao).mockResolvedValueOnce(undefined)
            renderPage()

            await waitFor(() => {
                expect(screen.getByRole('button', {name: 'Enviar p/ Validação'})).toBeEnabled()
            })
            fireEvent.click(screen.getByRole('button', {name: 'Enviar p/ Validação'}))
            expect(screen.getByText('Enviar para validação?')).toBeInTheDocument()
            fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', {name: 'Enviar'}))

            await waitFor(() => {
                expect(enviarParaValidacao).toHaveBeenCalledWith([7])
            })
            const {toast} = await import('sonner')
            expect(toast.success).toHaveBeenCalledWith('Operação concluída.')
            expect(await screen.findByText('lista')).toBeInTheDocument()
        })
    })

    describe('duplicar via ModalDuplicar (UC17-FE)', () => {
        it('renderiza ModalDuplicar (não ModalConfirmacao) pré-preenchido com os dados originais', async () => {
            vi.mocked(fetchAmbienteNaoPublicado).mockResolvedValue(AMBIENTE)
            renderPage()

            await waitFor(() => {
                expect(screen.getByRole('button', {name: 'Duplicar'})).toBeEnabled()
            })
            fireEvent.click(screen.getByRole('button', {name: 'Duplicar'}))

            expect(screen.getByText('Duplicar Ambiente')).toBeInTheDocument()
            expect(screen.getByLabelText('Nome')).toHaveValue('Sala 101')
            // ModalConfirmacao NÃO abre para 'duplicar'
            expect(screen.queryByText('Duplicar ambiente?')).not.toBeInTheDocument()
            expect(screen.queryByText('Será criada uma cópia do ambiente.')).not.toBeInTheDocument()
        })

        it('duplica, invalida a query e navega para o ambiente criado', async () => {
            vi.mocked(fetchAmbienteNaoPublicado).mockResolvedValue(AMBIENTE)
            vi.mocked(duplicarAmbiente).mockResolvedValue({...AMBIENTE, id: 8, nome: 'Sala 102'})
            const queryClient = createQueryClient()
            const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries')
            renderPage(undefined, queryClient)

            await waitFor(() => {
                expect(screen.getByRole('button', {name: 'Duplicar'})).toBeEnabled()
            })
            fireEvent.click(screen.getByRole('button', {name: 'Duplicar'}))
            fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', {name: 'Duplicar'}))

            await waitFor(() => {
                expect(duplicarAmbiente).toHaveBeenCalledWith(7, {
                    nome: 'Sala 101',
                    localizacao: {bloco: 'BLOCO_1', unidade: 'SEDE', andar: 2},
                })
            })
            const {toast} = await import('sonner')
            expect(toast.success).toHaveBeenCalledWith('Ambiente duplicado.')
            expect(invalidateSpy).toHaveBeenCalledWith({queryKey: ['ambientes', 'nao-publicados']})
            expect(await screen.findByText('detalhe-duplicado')).toBeInTheDocument()
        })

        it('mantém a página quando a duplicação falha (RN-1.7 corrigível no modal)', async () => {
            vi.mocked(fetchAmbienteNaoPublicado).mockResolvedValue(AMBIENTE)
            vi.mocked(duplicarAmbiente).mockRejectedValueOnce(
                Object.assign(new Error('conflict'), {
                    isAxiosError: true,
                    response: {
                        status: 409,
                        data: {mensagem: 'Já existe um ambiente com este nome nesta localização.'},
                    },
                }),
            )
            renderPage()

            await waitFor(() => {
                expect(screen.getByRole('button', {name: 'Duplicar'})).toBeEnabled()
            })
            fireEvent.click(screen.getByRole('button', {name: 'Duplicar'}))
            fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', {name: 'Duplicar'}))

            const {toast} = await import('sonner')
            await waitFor(() => {
                expect(toast.error).toHaveBeenCalledWith(
                    'Já existe um ambiente com este nome nesta localização.',
                )
            })
            // Modal permanece aberto e a página não navega
            expect(screen.getByText('Duplicar Ambiente')).toBeInTheDocument()
            expect(screen.queryByText('detalhe-duplicado')).not.toBeInTheDocument()
        })
    })
})
