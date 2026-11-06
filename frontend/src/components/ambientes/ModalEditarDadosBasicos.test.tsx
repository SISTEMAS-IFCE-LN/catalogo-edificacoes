import {fireEvent, render, screen, waitFor} from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {ModalEditarDadosBasicos} from './ModalEditarDadosBasicos'
import type {AmbienteDetalhe} from '@/types/ambientes/response'
import {StatusAmbiente, TipoAmbiente} from '@/types/ambientes/enums'

vi.mock('sonner', () => ({
    toast: {success: vi.fn(), error: vi.fn()},
}))

const mockAtualizarDadosBasicos = vi.hoisted(() => vi.fn())
vi.mock('@/lib/api/api-naopublicados', () => ({
    atualizarDadosBasicos: (...args: unknown[]) => mockAtualizarDadosBasicos(...args),
}))

// Referências fixas: o componente não precisa de QueryClient real nestes testes.
vi.mock('@/hooks/useReferencias', () => ({
    useCampus: () => ({
        data: [
            {id: 1, nome: 'LIMOEIRO DO NORTE'},
            {id: 2, nome: 'CIDADE ALTA'},
        ],
        isLoading: false,
    }),
    useUnidades: (campusId: number | null) => ({
        data: campusId === 2
            ? [{id: 3, nome: 'UNIDADE NORTE'}]
            : campusId === 1
                ? [{id: 1, nome: 'SEDE'}]
                : [],
        isLoading: false,
    }),
    useBlocos: (unidadeId: number | null) => ({
        data: unidadeId === 3
            ? [{id: 4, nome: 'BLOCO A'}]
            : unidadeId === 1
                ? [{id: 1, nome: 'BLOCO 1'}]
                : [],
        isLoading: false,
    }),
}))

const AMBIENTE: AmbienteDetalhe = {
    id: 7,
    nome: 'Sala 101',
    tipo: TipoAmbiente.SALA_AULA,
    localizacao: {
        id: 3,
        campus: {id: 1, nome: 'LIMOEIRO DO NORTE'},
        unidade: {id: 1, nome: 'SEDE'},
        bloco: {id: 1, nome: 'BLOCO 1'},
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

type PropsDoComponente = Parameters<typeof ModalEditarDadosBasicos>[0]

function renderModal(props: Partial<PropsDoComponente> = {}) {
    const onSalvou = vi.fn()
    const onOpenChange = vi.fn()

    render(
        <ModalEditarDadosBasicos
            open
            ambiente={AMBIENTE}
            onOpenChange={onOpenChange}
            onSalvou={onSalvou}
            {...props}
        />,
    )

    return {onSalvou, onOpenChange}
}

describe('ModalEditarDadosBasicos (UC07-FE)', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    describe('pré-preenchimento', () => {
        it('preenche nome, capacidade e andar com os dados do ambiente', () => {
            renderModal()
            expect(screen.getByLabelText('Nome')).toHaveValue('Sala 101')
            expect(screen.getByLabelText('Capacidade')).toHaveValue(30)
            expect(screen.getByLabelText('Andar')).toHaveValue(2)
        })

        it('pré-seleciona campus, unidade e bloco pelos IDs da resposta', () => {
            renderModal()
            expect(screen.getByLabelText('Campus')).toHaveTextContent('LIMOEIRO DO NORTE')
            expect(screen.getByLabelText('Unidade')).toHaveTextContent('SEDE')
            expect(screen.getByLabelText('Bloco')).toHaveTextContent('BLOCO 1')
        })
    })

    describe('validação client-side', () => {
        it('não submete com nome vazio e exibe mensagem', async () => {
            renderModal()
            fireEvent.change(screen.getByLabelText('Nome'), {target: {value: ''}})
            fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

            await waitFor(() => {
                expect(screen.getByRole('alert')).toHaveTextContent('Nome obrigatório.')
            })
            expect(mockAtualizarDadosBasicos).not.toHaveBeenCalled()
        })

        it('não submete com capacidade 0 e exibe mensagem', async () => {
            renderModal()
            fireEvent.change(screen.getByLabelText('Capacidade'), {target: {value: '0'}})
            fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

            await waitFor(() => {
                expect(screen.getByRole('alert')).toBeInTheDocument()
            })
            expect(mockAtualizarDadosBasicos).not.toHaveBeenCalled()
        })

        it('não submete com andar vazio e exibe erro', async () => {
            renderModal()
            fireEvent.change(screen.getByLabelText('Andar'), {target: {value: ''}})
            fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

            await waitFor(() => {
                expect(screen.getByRole('alert')).toBeInTheDocument()
            })
            expect(mockAtualizarDadosBasicos).not.toHaveBeenCalled()
        })
    })

    describe('sucesso', () => {
        it('chama atualizarDadosBasicos com o payload aninhado (IDs existentes)', async () => {
            mockAtualizarDadosBasicos.mockResolvedValueOnce(undefined)
            const {onSalvou, onOpenChange} = renderModal()

            fireEvent.change(screen.getByLabelText('Nome'), {target: {value: 'Sala 102'}})
            fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

            await waitFor(() => {
                expect(mockAtualizarDadosBasicos).toHaveBeenCalledWith(7, {
                    nome: 'Sala 102',
                    capacidade: 30,
                    // LocalizacaoReq não tem id de localização; cada nível usa
                    // {id} para existente ou {nome} para find-or-create.
                    localizacao: {
                        campus: {id: 1, nome: null},
                        unidade: {id: 1, nome: null},
                        bloco: {id: 1, nome: null},
                        andar: 2,
                    },
                })
            })
            const {toast} = await import('sonner')
            expect(toast.success).toHaveBeenCalledWith('Dados básicos atualizados.')
            expect(onSalvou).toHaveBeenCalledTimes(1)
            expect(onOpenChange).toHaveBeenCalledWith(false)
        })

        it('trocar o campus reseta unidade/bloco e envia a nova cascata', async () => {
            mockAtualizarDadosBasicos.mockResolvedValueOnce(undefined)
            const user = userEvent.setup()
            renderModal()

            await user.click(screen.getByLabelText('Campus'))
            await user.click(await screen.findByRole('option', {name: 'CIDADE ALTA'}))

            expect(screen.getByLabelText('Unidade')).not.toHaveTextContent('SEDE')
            expect(screen.getByLabelText('Bloco')).not.toHaveTextContent('BLOCO 1')

            await user.click(screen.getByLabelText('Unidade'))
            await user.click(await screen.findByRole('option', {name: 'UNIDADE NORTE'}))
            await user.click(screen.getByLabelText('Bloco'))
            await user.click(await screen.findByRole('option', {name: 'BLOCO A'}))
            await user.click(screen.getByRole('button', {name: 'Salvar'}))

            await waitFor(() => {
                expect(mockAtualizarDadosBasicos).toHaveBeenCalledWith(7, {
                    nome: 'Sala 101',
                    capacidade: 30,
                    localizacao: {
                        campus: {id: 2, nome: null},
                        unidade: {id: 3, nome: null},
                        bloco: {id: 4, nome: null},
                        andar: 2,
                    },
                })
            })
        })

        it('campus novo força o cadastro da unidade e, depois, do bloco', async () => {
            const user = userEvent.setup()
            renderModal()

            await user.click(screen.getByLabelText('Campus'))
            await user.click(await screen.findByRole('option', {name: '+ Cadastrar novo'}))
            await user.type(screen.getByLabelText('Campus (novo)'), 'CAMPUS NOVO')

            // Campus ainda sem id: a unidade só pode ser criada por nome.
            expect(screen.getByLabelText('Unidade (novo)')).toBeInTheDocument()
            expect(screen.queryByLabelText('Unidade')).not.toBeInTheDocument()
            // Sem unidade resolvida, o bloco continua desabilitado (select).
            expect(screen.getByLabelText('Bloco')).toBeDisabled()

            await user.type(screen.getByLabelText('Unidade (novo)'), 'UNIDADE NOVA')

            expect(screen.getByLabelText('Bloco (novo)')).toBeInTheDocument()
            expect(screen.queryByLabelText('Bloco')).not.toBeInTheDocument()
        })

        it('envia o nome trimado no payload', async () => {
            mockAtualizarDadosBasicos.mockResolvedValueOnce(undefined)
            renderModal()

            fireEvent.change(screen.getByLabelText('Nome'), {target: {value: '  Sala 102  '}})
            fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

            await waitFor(() => {
                expect(mockAtualizarDadosBasicos).toHaveBeenCalledWith(
                    7,
                    expect.objectContaining({nome: 'Sala 102'}),
                )
            })
        })
    })

    it('erro do backend exibe ErroRes.mensagem e mantém o modal aberto', async () => {
        mockAtualizarDadosBasicos.mockRejectedValueOnce(
            Object.assign(new Error('Request failed'), {
                isAxiosError: true,
                response: {status: 409, data: {mensagem: 'Já existe um ambiente com este nome nesta localização.'}},
            }),
        )
        const {onOpenChange} = renderModal()

        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        const {toast} = await import('sonner')
        await waitFor(() => {
            expect(toast.error).toHaveBeenCalledWith('Já existe um ambiente com este nome nesta localização.')
        })
        expect(onOpenChange).not.toHaveBeenCalled()
        expect(screen.getByText('Editar Dados Básicos')).toBeInTheDocument()
    })

    it('cancelar fecha sem chamar a API', () => {
        const {onOpenChange} = renderModal()

        fireEvent.click(screen.getByRole('button', {name: 'Cancelar'}))

        expect(onOpenChange).toHaveBeenCalledWith(false)
        expect(mockAtualizarDadosBasicos).not.toHaveBeenCalled()
    })
})
