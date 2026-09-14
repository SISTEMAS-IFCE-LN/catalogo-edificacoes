import {fireEvent, render, screen, waitFor} from '@testing-library/react'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {ModalInfoAdicional} from './ModalInfoAdicional'
import type {AmbienteDetalhe} from '@/types/ambientes/response'
import {Bloco, StatusAmbiente, TipoAmbiente, Unidade} from '@/types/ambientes/enums'

vi.mock('sonner', () => ({
    toast: {success: vi.fn(), error: vi.fn()},
}))

const mockAtualizarInfoAdicional = vi.hoisted(() => vi.fn())
vi.mock('@/lib/api/api-naopublicados', () => ({
    atualizarInfoAdicional: (...args: unknown[]) => mockAtualizarInfoAdicional(...args),
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
    informacaoAdicional: 'Sala com projetor',
    status: StatusAmbiente.NAO_PUBLICADO,
}

type PropsDoComponente = Parameters<typeof ModalInfoAdicional>[0]

function renderModal(props: Partial<PropsDoComponente> = {}) {
    const onSalvou = vi.fn()
    const onOpenChange = vi.fn()

    render(
        <ModalInfoAdicional
            open
            ambiente={AMBIENTE}
            onOpenChange={onOpenChange}
            onSalvou={onSalvou}
            {...props}
        />,
    )

    return {onSalvou, onOpenChange}
}

describe('ModalInfoAdicional (UC14-FE)', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('pré-preenche a textarea com a informação atual e o contador', () => {
        renderModal()
        expect(screen.getByLabelText('Informação Adicional (opcional)')).toHaveValue('Sala com projetor')
        // O contador é dividido em três nós de texto ({length}/{max}) — compara o textContent
        expect(screen.getByText((_, el) => el?.tagName === 'P' && el.textContent === '17/255')).toBeInTheDocument()
    })

    it('salva com o campo vazio (opcional) e envia string crua para a API', async () => {
        mockAtualizarInfoAdicional.mockResolvedValueOnce(undefined)
        const {onSalvou, onOpenChange} = renderModal({ambiente: {...AMBIENTE, informacaoAdicional: ''}})

        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            // String CRUA como payload — a camada de API aplica Content-Type text/plain
            expect(mockAtualizarInfoAdicional).toHaveBeenCalledWith(7, '')
        })
        const {toast} = await import('sonner')
        expect(toast.success).toHaveBeenCalledWith('Informação adicional atualizada.')
        expect(onSalvou).toHaveBeenCalledTimes(1)
        expect(onOpenChange).toHaveBeenCalledWith(false)
    })

    it('submete o texto editado e atualiza o contador', async () => {
        mockAtualizarInfoAdicional.mockResolvedValueOnce(undefined)
        renderModal()

        const textarea = screen.getByLabelText('Informação Adicional (opcional)')
        fireEvent.change(textarea, {target: {value: 'Sala com ar-condicionado'}})
        expect(screen.getByText((_, el) => el?.tagName === 'P' && el.textContent === '24/255')).toBeInTheDocument()
        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            expect(mockAtualizarInfoAdicional).toHaveBeenCalledWith(7, 'Sala com ar-condicionado')
        })
    })

    it('bloqueia submit acima de 255 caracteres e exibe erro', async () => {
        renderModal({ambiente: {...AMBIENTE, informacaoAdicional: ''}})

        fireEvent.change(screen.getByLabelText('Informação Adicional (opcional)'), {
            target: {value: 'a'.repeat(256)},
        })
        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            expect(screen.getByRole('alert')).toBeInTheDocument()
        })
        expect(mockAtualizarInfoAdicional).not.toHaveBeenCalled()
    })

    it('erro do backend mantém o conteúdo editável e o modal aberto', async () => {
        mockAtualizarInfoAdicional.mockRejectedValueOnce(
            Object.assign(new Error('Request failed'), {
                isAxiosError: true,
                response: {status: 400, data: {mensagem: 'Texto inválido.'}},
            }),
        )
        const {onOpenChange} = renderModal()

        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        const {toast} = await import('sonner')
        await waitFor(() => {
            expect(toast.error).toHaveBeenCalledWith('Texto inválido.')
        })
        expect(onOpenChange).not.toHaveBeenCalled()
        expect(screen.getByLabelText('Informação Adicional (opcional)')).toHaveValue('Sala com projetor')
    })

    it('cancelar fecha sem chamar a API', () => {
        const {onOpenChange} = renderModal()

        fireEvent.click(screen.getByRole('button', {name: 'Cancelar'}))

        expect(onOpenChange).toHaveBeenCalledWith(false)
        expect(mockAtualizarInfoAdicional).not.toHaveBeenCalled()
    })
})
