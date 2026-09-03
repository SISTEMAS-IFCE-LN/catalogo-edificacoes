import {fireEvent, render, screen, waitFor} from '@testing-library/react'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import type {ComponentProps} from 'react'
import {ModalDuplicar} from './ModalDuplicar'
import type {AmbienteDetalhe} from '@/types/ambientes/response'
import {Bloco, StatusAmbiente, TipoAmbiente, Unidade} from '@/types/ambientes/enums'

vi.mock('sonner', () => ({
    toast: {success: vi.fn(), error: vi.fn()},
}))

const mockDuplicarAmbiente = vi.hoisted(() => vi.fn())
vi.mock('@/lib/api/api-naopublicados', () => ({
    duplicarAmbiente: (...args: unknown[]) => mockDuplicarAmbiente(...args),
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

// Resposta 201 do backend para a duplicação
const NOVO_AMBIENTE: AmbienteDetalhe = {...AMBIENTE, id: 8, nome: 'Sala 102'}

// Erro no formato do interceptor (ErroRes.mensagem) — RN-1.7, por exemplo
function erroDoBackend(mensagem: string): Error {
    return Object.assign(new Error('Request failed'), {
        isAxiosError: true,
        response: {status: 409, data: {mensagem}},
    })
}

type PropsDoComponente = ComponentProps<typeof ModalDuplicar>

function renderModal(props: Partial<PropsDoComponente> = {}) {
    return render(
        <ModalDuplicar
            open
            ambiente={AMBIENTE}
            onOpenChange={vi.fn()}
            onSalvou={vi.fn()}
            {...props}
        />,
    )
}

describe('ModalDuplicar', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    describe('pré-preenchimento', () => {
        it('preenche nome e andar com os dados ORIGINAIS, sem sufixo "(cópia)"', () => {
            renderModal()
            expect(screen.getByLabelText('Nome')).toHaveValue('Sala 101')
            expect(screen.getByLabelText('Andar')).toHaveValue(2)
            expect(screen.queryByText(/cópia/i)).not.toBeInTheDocument()
        })

        it('converte rótulos da resposta para nomes técnicos nos selects', () => {
            renderModal()
            // Sem `items` no Select, o trigger renderiza o valor bruto (a chave técnica)
            expect(screen.getByLabelText('Bloco')).toHaveTextContent('BLOCO_1')
            expect(screen.getByLabelText('Unidade')).toHaveTextContent('SEDE')
        })
    })

    describe('validação client-side', () => {
        it('não submete com nome vazio e exibe mensagem', async () => {
            renderModal()
            fireEvent.change(screen.getByLabelText('Nome'), {target: {value: ''}})
            fireEvent.click(screen.getByRole('button', {name: 'Duplicar'}))

            await waitFor(() =>
                expect(screen.getByRole('alert')).toHaveTextContent('Nome obrigatório.'),
            )
            expect(mockDuplicarAmbiente).not.toHaveBeenCalled()
        })

        it('não submete com nome contendo só espaços (trim) e exibe mensagem', async () => {
            renderModal()
            fireEvent.change(screen.getByLabelText('Nome'), {target: {value: '   '}})
            fireEvent.click(screen.getByRole('button', {name: 'Duplicar'}))

            await waitFor(() =>
                expect(screen.getByRole('alert')).toHaveTextContent('Nome obrigatório.'),
            )
            expect(mockDuplicarAmbiente).not.toHaveBeenCalled()
        })

        it('não submete com nome acima de 50 caracteres e exibe mensagem', async () => {
            renderModal()
            fireEvent.change(screen.getByLabelText('Nome'), {target: {value: 'a'.repeat(51)}})
            fireEvent.click(screen.getByRole('button', {name: 'Duplicar'}))

            await waitFor(() =>
                expect(screen.getByRole('alert')).toHaveTextContent('Máximo 50 caracteres.'),
            )
            expect(mockDuplicarAmbiente).not.toHaveBeenCalled()
        })

        it('não submete com localização inválida (andar vazio) e exibe erro', async () => {
            renderModal()
            fireEvent.change(screen.getByLabelText('Andar'), {target: {value: ''}})
            fireEvent.click(screen.getByRole('button', {name: 'Duplicar'}))

            await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument())
            expect(mockDuplicarAmbiente).not.toHaveBeenCalled()
        })
    })

    describe('sucesso', () => {
        it('chama duplicarAmbiente com os valores editados, toasta e chama onSalvou com o novo id', async () => {
            mockDuplicarAmbiente.mockResolvedValueOnce(NOVO_AMBIENTE)
            const onSalvou = vi.fn()
            const onOpenChange = vi.fn()
            renderModal({onSalvou, onOpenChange})

            fireEvent.change(screen.getByLabelText('Nome'), {target: {value: 'Sala 102'}})
            fireEvent.click(screen.getByRole('button', {name: 'Duplicar'}))

            await waitFor(() =>
                expect(mockDuplicarAmbiente).toHaveBeenCalledWith(7, {
                    nome: 'Sala 102',
                    // Chaves técnicas convertidas a partir dos rótulos pré-preenchidos
                    localizacao: {bloco: 'BLOCO_1', unidade: 'SEDE', andar: 2},
                }),
            )
            const {toast} = await import('sonner')
            expect(toast.success).toHaveBeenCalledWith('Ambiente duplicado.')
            expect(onSalvou).toHaveBeenCalledWith(8)
            expect(onOpenChange).toHaveBeenCalledWith(false)
        })

        it('envia o nome trimado no payload', async () => {
            mockDuplicarAmbiente.mockResolvedValueOnce(NOVO_AMBIENTE)
            renderModal()

            fireEvent.change(screen.getByLabelText('Nome'), {target: {value: '  Sala 102  '}})
            fireEvent.click(screen.getByRole('button', {name: 'Duplicar'}))

            await waitFor(() =>
                expect(mockDuplicarAmbiente).toHaveBeenCalledWith(
                    7,
                    expect.objectContaining({nome: 'Sala 102'}),
                ),
            )
        })
    })

    describe('erro do backend (useAsyncAction)', () => {
        it('exibe ErroRes.mensagem e mantém o modal aberto', async () => {
            mockDuplicarAmbiente.mockRejectedValueOnce(
                erroDoBackend('Já existe um ambiente com este nome nesta localização.'),
            )
            const onOpenChange = vi.fn()
            renderModal({onOpenChange})

            fireEvent.click(screen.getByRole('button', {name: 'Duplicar'}))

            const {toast} = await import('sonner')
            await waitFor(() =>
                expect(toast.error).toHaveBeenCalledWith(
                    'Já existe um ambiente com este nome nesta localização.',
                ),
            )
            // Modal permanece aberto: onOpenChange(false) não é chamado
            expect(onOpenChange).not.toHaveBeenCalled()
            expect(screen.getByText('Duplicar Ambiente')).toBeInTheDocument()
        })

        it('exibe mensagem padrão quando o erro não tem ErroRes.mensagem', async () => {
            mockDuplicarAmbiente.mockRejectedValueOnce(new Error('rede caiu'))
            renderModal()

            fireEvent.click(screen.getByRole('button', {name: 'Duplicar'}))

            const {toast} = await import('sonner')
            await waitFor(() =>
                expect(toast.error).toHaveBeenCalledWith('Erro ao duplicar ambiente.'),
            )
        })
    })

    it('chama onOpenChange(false) ao cancelar', () => {
        const onOpenChange = vi.fn()
        renderModal({onOpenChange})

        fireEvent.click(screen.getByRole('button', {name: 'Cancelar'}))
        expect(onOpenChange).toHaveBeenCalledWith(false)
        expect(mockDuplicarAmbiente).not.toHaveBeenCalled()
    })
})
