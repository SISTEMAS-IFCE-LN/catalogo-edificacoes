import {fireEvent, render, screen, waitFor, within} from '@testing-library/react'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {MemoryRouter, Route, Routes} from 'react-router'
import {QueryClient, QueryClientProvider} from '@tanstack/react-query'
import {NaoPublicadoDetalhePage} from './page'
import type {AmbienteDetalhe} from '@/types/ambientes/response'
import type {User} from '@/types/usuarios/user'
import {Role} from '@/types/usuarios/user'
import {
    Bloco,
    MaterialEsquadria,
    StatusAmbiente,
    TipoAmbiente,
    TipoEsquadria,
    TipoGeometria,
    Unidade,
} from '@/types/ambientes/enums'

vi.mock('@/lib/api/api-naopublicados', () => ({
    fetchAmbienteNaoPublicado: vi.fn(),
    deletarAmbientes: vi.fn(),
    enviarParaValidacao: vi.fn(),
    duplicarAmbiente: vi.fn(),
    atualizarDadosBasicos: vi.fn(),
    incluirGeometrias: vi.fn(),
    atualizarGeometrias: vi.fn(),
    incluirPesDireitos: vi.fn(),
    atualizarPesDireitos: vi.fn(),
    incluirEsquadrias: vi.fn(),
    atualizarEsquadrias: vi.fn(),
    atualizarInfoAdicional: vi.fn(),
    alterarTipo: vi.fn(),
}))

vi.mock('sonner', () => ({
    toast: {success: vi.fn(), error: vi.fn()},
}))

vi.mock('@/hooks/useAuth', () => ({
    useAuth: vi.fn(),
}))

import {
    alterarTipo,
    atualizarDadosBasicos,
    atualizarEsquadrias,
    atualizarGeometrias,
    atualizarInfoAdicional,
    atualizarPesDireitos,
    deletarAmbientes,
    duplicarAmbiente,
    enviarParaValidacao,
    fetchAmbienteNaoPublicado,
    incluirGeometrias,
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

// Ambiente com geometrias/esquadrias preenchidas — usado pelos modais de
// edição (UC09/UC11/UC13/UC16), cujo pré-preenchimento precisa de conteúdo.
const AMBIENTE_COMPLETO: AmbienteDetalhe = {
    ...AMBIENTE,
    geometrias: [
        {id: 10, tipo: TipoGeometria.RETANGULAR, base: 4, altura: 3, repeticao: 2, area: 24},
    ],
    areaAmbiente: 24,
    esquadriasDetalhes: {
        esquadrias: [
            {
                id: 11,
                tipo: TipoEsquadria.PORTA,
                geometria: {id: 12, base: 0.9, altura: 2.1, repeticao: 1, area: 1.89},
                alturaPeitoril: 0,
                area: 1.89,
                material: MaterialEsquadria.ALUMINIO,
                informacaoAdicional: '',
            },
        ],
        esquadriasTipoMaterial: [],
    },
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

    // O texto da etapa do wizard é dividido entre nós de texto e um <strong>
    // — compara o textContent do wrapper (ver FormAmbiente.test.tsx).
    function buscarEtapa(n: number, nome: string) {
        const alvo = `Etapa ${n} de 5: ${nome}`
        return screen.queryByText((_, el) => el?.tagName === 'DIV' && el.textContent === alvo)
    }

    async function avancarEtapa(n: number, nome: string) {
        const dialogo = within(screen.getByRole('dialog'))
        fireEvent.click(dialogo.getByRole('button', {name: 'Próximo'}))
        await waitFor(() => expect(buscarEtapa(n, nome)).not.toBeNull())
    }

    describe('editar dados básicos via ModalEditarDadosBasicos (UC07-FE)', () => {
        it('abre pré-preenchido, salva e invalida o detalhe', async () => {
            vi.mocked(fetchAmbienteNaoPublicado).mockResolvedValue(AMBIENTE)
            vi.mocked(atualizarDadosBasicos).mockResolvedValueOnce(undefined)
            const queryClient = createQueryClient()
            const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries')
            renderPage(undefined, queryClient)

            await waitFor(() => {
                expect(screen.getByRole('button', {name: 'Editar Dados Básicos'})).toBeEnabled()
            })
            fireEvent.click(screen.getByRole('button', {name: 'Editar Dados Básicos'}))

            const dialogo = within(screen.getByRole('dialog'))
            expect(dialogo.getByText('Editar Dados Básicos')).toBeInTheDocument()
            expect(dialogo.getByLabelText('Nome')).toHaveValue('Sala 101')
            fireEvent.change(dialogo.getByLabelText('Nome'), {target: {value: 'Sala 102'}})
            fireEvent.click(dialogo.getByRole('button', {name: 'Salvar'}))

            await waitFor(() => {
                expect(atualizarDadosBasicos).toHaveBeenCalledWith(7, {
                    nome: 'Sala 102',
                    capacidade: 30,
                    localizacao: {bloco: 'BLOCO_1', unidade: 'SEDE', andar: 2},
                })
            })
            const {toast} = await import('sonner')
            expect(toast.success).toHaveBeenCalledWith('Dados básicos atualizados.')
            expect(invalidateSpy).toHaveBeenCalledWith({
                queryKey: ['ambientes', 'nao-publicados', 'detalhe', '7'],
            })
        })

        it('mantém o modal aberto em erro do backend (mensagem corrigível)', async () => {
            vi.mocked(fetchAmbienteNaoPublicado).mockResolvedValue(AMBIENTE)
            vi.mocked(atualizarDadosBasicos).mockRejectedValueOnce(
                Object.assign(new Error('conflict'), {
                    isAxiosError: true,
                    response: {status: 409, data: {mensagem: 'Já existe um ambiente com este nome nesta localização.'}},
                }),
            )
            renderPage()

            await waitFor(() => {
                expect(screen.getByRole('button', {name: 'Editar Dados Básicos'})).toBeEnabled()
            })
            fireEvent.click(screen.getByRole('button', {name: 'Editar Dados Básicos'}))
            fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', {name: 'Salvar'}))

            const {toast} = await import('sonner')
            await waitFor(() => {
                expect(toast.error).toHaveBeenCalledWith('Já existe um ambiente com este nome nesta localização.')
            })
            // O título existe no botão da página E no dialog — consulta dentro do dialog
            expect(within(screen.getByRole('dialog')).getByText('Editar Dados Básicos')).toBeInTheDocument()
        })
    })

    describe('incluir geometrias via ModalGeometrias (UC08-FE)', () => {
        it('abre com card em branco, salva e invalida o detalhe', async () => {
            vi.mocked(fetchAmbienteNaoPublicado).mockResolvedValue(AMBIENTE)
            vi.mocked(incluirGeometrias).mockResolvedValueOnce(undefined)
            const queryClient = createQueryClient()
            const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries')
            renderPage(undefined, queryClient)

            await waitFor(() => {
                expect(screen.getByRole('button', {name: 'Incluir Geometrias'})).toBeEnabled()
            })
            fireEvent.click(screen.getByRole('button', {name: 'Incluir Geometrias'}))

            const dialogo = within(screen.getByRole('dialog'))
            fireEvent.change(dialogo.getByLabelText('Base (m)'), {target: {value: '4'}})
            fireEvent.change(dialogo.getByLabelText('Altura (m)'), {target: {value: '3'}})
            fireEvent.click(dialogo.getByRole('button', {name: 'Salvar'}))

            await waitFor(() => {
                expect(incluirGeometrias).toHaveBeenCalledWith(7, [
                    {tipo: 'RETANGULAR', base: 4, altura: 3, repeticao: 1},
                ])
            })
            const {toast} = await import('sonner')
            expect(toast.success).toHaveBeenCalledWith('Geometrias incluídas.')
            expect(invalidateSpy).toHaveBeenCalledWith({
                queryKey: ['ambientes', 'nao-publicados', 'detalhe', '7'],
            })
        })
    })

    describe('editar geometrias via ModalGeometrias (UC09-FE)', () => {
        it('abre pré-preenchido com as geometrias atuais (nomes técnicos) e salva', async () => {
            vi.mocked(fetchAmbienteNaoPublicado).mockResolvedValue(AMBIENTE_COMPLETO)
            vi.mocked(atualizarGeometrias).mockResolvedValueOnce(undefined)
            renderPage()

            await waitFor(() => {
                expect(screen.getByRole('button', {name: 'Editar Geometrias'})).toBeEnabled()
            })
            fireEvent.click(screen.getByRole('button', {name: 'Editar Geometrias'}))

            const dialogo = within(screen.getByRole('dialog'))
            expect(dialogo.getByLabelText('Base (m)')).toHaveValue(4)
            expect(dialogo.getByLabelText('Altura (m)')).toHaveValue(3)
            fireEvent.click(dialogo.getByRole('button', {name: 'Salvar'}))

            await waitFor(() => {
                expect(atualizarGeometrias).toHaveBeenCalledWith(7, [
                    {tipo: 'RETANGULAR', base: 4, altura: 3, repeticao: 2},
                ])
            })
            const {toast} = await import('sonner')
            expect(toast.success).toHaveBeenCalledWith('Geometrias atualizadas.')
        })
    })

    describe('editar pés-direitos via ModalPesDireitos (UC11-FE)', () => {
        it('abre pré-preenchido com as alturas e salva', async () => {
            vi.mocked(fetchAmbienteNaoPublicado).mockResolvedValue(AMBIENTE)
            vi.mocked(atualizarPesDireitos).mockResolvedValueOnce(undefined)
            renderPage()

            await waitFor(() => {
                expect(screen.getByRole('button', {name: 'Editar Pés-direitos'})).toBeEnabled()
            })
            fireEvent.click(screen.getByRole('button', {name: 'Editar Pés-direitos'}))

            const dialogo = within(screen.getByRole('dialog'))
            expect(dialogo.getByLabelText('Pé-direito 1 (m)')).toHaveValue(3)
            fireEvent.click(dialogo.getByRole('button', {name: 'Salvar'}))

            await waitFor(() => {
                expect(atualizarPesDireitos).toHaveBeenCalledWith(7, [3])
            })
            const {toast} = await import('sonner')
            expect(toast.success).toHaveBeenCalledWith('Pés-direitos atualizados.')
        })
    })

    describe('editar esquadrias via ModalEsquadrias (UC13-FE)', () => {
        it('abre pré-preenchido (nomes técnicos) e salva', async () => {
            vi.mocked(fetchAmbienteNaoPublicado).mockResolvedValue(AMBIENTE_COMPLETO)
            vi.mocked(atualizarEsquadrias).mockResolvedValueOnce(undefined)
            renderPage()

            await waitFor(() => {
                expect(screen.getByRole('button', {name: 'Editar Esquadrias'})).toBeEnabled()
            })
            fireEvent.click(screen.getByRole('button', {name: 'Editar Esquadrias'}))

            const dialogo = within(screen.getByRole('dialog'))
            expect(dialogo.getByLabelText('Base (m)')).toHaveValue(0.9)
            fireEvent.click(dialogo.getByRole('button', {name: 'Salvar'}))

            await waitFor(() => {
                expect(atualizarEsquadrias).toHaveBeenCalledWith(7, [
                    {
                        tipo: 'PORTA',
                        material: 'ALUMINIO',
                        geometria: {base: 0.9, altura: 2.1, repeticao: 1},
                        alturaPeitoril: 0,
                        informacaoAdicional: '',
                    },
                ])
            })
            const {toast} = await import('sonner')
            expect(toast.success).toHaveBeenCalledWith('Esquadrias atualizadas.')
        })
    })

    describe('info adicional via ModalInfoAdicional (UC14-FE)', () => {
        it('salva o texto editado e invalida o detalhe', async () => {
            vi.mocked(fetchAmbienteNaoPublicado).mockResolvedValue(AMBIENTE)
            vi.mocked(atualizarInfoAdicional).mockResolvedValueOnce(undefined)
            const queryClient = createQueryClient()
            const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries')
            renderPage(undefined, queryClient)

            await waitFor(() => {
                expect(screen.getByRole('button', {name: 'Info Adicional'})).toBeEnabled()
            })
            fireEvent.click(screen.getByRole('button', {name: 'Info Adicional'}))

            const dialogo = within(screen.getByRole('dialog'))
            fireEvent.change(dialogo.getByLabelText('Informação Adicional (opcional)'), {
                target: {value: 'Sala com ar-condicionado'},
            })
            fireEvent.click(dialogo.getByRole('button', {name: 'Salvar'}))

            await waitFor(() => {
                expect(atualizarInfoAdicional).toHaveBeenCalledWith(7, 'Sala com ar-condicionado')
            })
            const {toast} = await import('sonner')
            expect(toast.success).toHaveBeenCalledWith('Informação adicional atualizada.')
            expect(invalidateSpy).toHaveBeenCalledWith({
                queryKey: ['ambientes', 'nao-publicados', 'detalhe', '7'],
            })
        })
    })

    describe('alterar tipo via ModalAlterarTipo (UC16-FE)', () => {
        it('abre o wizard pré-preenchido, submete o AmbienteReq completo e navega para o novo id', async () => {
            vi.mocked(fetchAmbienteNaoPublicado).mockResolvedValue(AMBIENTE_COMPLETO)
            vi.mocked(alterarTipo).mockResolvedValueOnce({...AMBIENTE_COMPLETO, id: 8})
            const queryClient = createQueryClient()
            const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries')
            renderPage(undefined, queryClient)

            await waitFor(() => {
                expect(screen.getByRole('button', {name: 'Alterar Tipo'})).toBeEnabled()
            })
            fireEvent.click(screen.getByRole('button', {name: 'Alterar Tipo'}))

            const dialogo = within(screen.getByRole('dialog'))
            expect(dialogo.getByText(/cria um novo ambiente e remove o antigo/)).toBeInTheDocument()
            expect(dialogo.getByLabelText('Nome')).toHaveValue('Sala 101')

            await avancarEtapa(2, 'Geometrias')
            await avancarEtapa(3, 'Pés-direitos')
            await avancarEtapa(4, 'Esquadrias')
            await avancarEtapa(5, 'Informação Adicional')
            fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', {name: 'Salvar'}))

            await waitFor(() => {
                expect(alterarTipo).toHaveBeenCalledWith(7, {
                    nome: 'Sala 101',
                    tipo: 'SALA_AULA',
                    capacidade: 30,
                    localizacao: {bloco: 'BLOCO_1', unidade: 'SEDE', andar: 2},
                    geometrias: [{tipo: 'RETANGULAR', base: 4, altura: 3, repeticao: 2}],
                    pesDireitos: [3],
                    esquadrias: [
                        {
                            tipo: 'PORTA',
                            material: 'ALUMINIO',
                            geometria: {base: 0.9, altura: 2.1, repeticao: 1},
                            alturaPeitoril: 0,
                            informacaoAdicional: '',
                        },
                    ],
                    informacaoAdicional: '',
                })
            })
            const {toast} = await import('sonner')
            expect(toast.success).toHaveBeenCalledWith('Tipo alterado.')
            // UC16: ambiente antigo deixa de existir → invalida a LISTA e navega
            expect(invalidateSpy).toHaveBeenCalledWith({queryKey: ['ambientes', 'nao-publicados']})
            expect(await screen.findByText('detalhe-duplicado')).toBeInTheDocument()
        })
    })
})
