import {fireEvent, render, screen, waitFor} from '@testing-library/react'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {ModalEditarDadosBasicos} from './ModalEditarDadosBasicos'
import type {AmbienteDetalhe} from '@/types/ambientes/response'
import {Bloco, StatusAmbiente, TipoAmbiente, Unidade} from '@/types/ambientes/enums'

vi.mock('sonner', () => ({
    toast: {success: vi.fn(), error: vi.fn()},
}))

const mockAtualizarDadosBasicos = vi.hoisted(() => vi.fn())
vi.mock('@/lib/api/api-naopublicados', () => ({
    atualizarDadosBasicos: (...args: unknown[]) => mockAtualizarDadosBasicos(...args),
}))

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

        it('converte rótulos da resposta para nomes técnicos nos selects', () => {
            renderModal()
            expect(screen.getByLabelText('Bloco')).toHaveTextContent('BLOCO_1')
            expect(screen.getByLabelText('Unidade')).toHaveTextContent('SEDE')
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
        it('chama atualizarDadosBasicos com o payload sem id na localizacao', async () => {
            mockAtualizarDadosBasicos.mockResolvedValueOnce(undefined)
            const {onSalvou, onOpenChange} = renderModal()

            fireEvent.change(screen.getByLabelText('Nome'), {target: {value: 'Sala 102'}})
            fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

            await waitFor(() => {
                expect(mockAtualizarDadosBasicos).toHaveBeenCalledWith(7, {
                    nome: 'Sala 102',
                    capacidade: 30,
                    // LocalizacaoReq NÃO tem id — enviar { bloco, unidade, andar }
                    localizacao: {bloco: 'BLOCO_1', unidade: 'SEDE', andar: 2},
                })
            })
            const {toast} = await import('sonner')
            expect(toast.success).toHaveBeenCalledWith('Dados básicos atualizados.')
            expect(onSalvou).toHaveBeenCalledTimes(1)
            expect(onOpenChange).toHaveBeenCalledWith(false)
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
