import {fireEvent, render, screen, waitFor} from '@testing-library/react'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {ModalEsquadrias} from './ModalEsquadrias'
import type {EsquadriaInput} from '@/types/ambientes/request'

vi.mock('sonner', () => ({
    toast: {success: vi.fn(), error: vi.fn()},
}))

type PropsDoComponente = Parameters<typeof ModalEsquadrias>[0]

const PREenchidas: EsquadriaInput[] = [
    {
        tipo: 'PORTA',
        geometria: {base: 0.9, altura: 2.1, repeticao: 1},
        material: 'ALUMINIO',
        alturaPeitoril: 0,
        informacaoAdicional: 'Porta dupla',
    },
    {
        tipo: 'JANELA',
        geometria: {base: 1.5, altura: 1.2, repeticao: 3},
        material: 'FERRO_VIDRO',
        alturaPeitoril: 1.1,
        informacaoAdicional: '',
    },
]

function renderModal(props: Partial<PropsDoComponente> = {}) {
    const onSubmit = vi.fn<PropsDoComponente['onSubmit']>().mockResolvedValue(undefined)
    const onSalvou = vi.fn()
    const onOpenChange = vi.fn()

    render(
        <ModalEsquadrias
            open
            modo="incluir"
            titulo="Incluir Esquadrias"
            inicial={[]}
            onOpenChange={onOpenChange}
            onSubmit={onSubmit}
            onSalvou={onSalvou}
            {...props}
        />,
    )

    return {onSubmit, onSalvou, onOpenChange}
}

describe('ModalEsquadrias (UC12/UC13-FE)', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('modo incluir inicia com 1 card em branco (porta, material NAO_SE_APLICA)', () => {
        renderModal()
        expect(screen.getByText('Esquadria 1')).toBeInTheDocument()
        expect(screen.queryByText('Esquadria 2')).not.toBeInTheDocument()
    })

    it('modo editar pré-preenche os valores recebidos (nomes técnicos)', () => {
        renderModal({modo: 'editar', titulo: 'Editar Esquadrias', inicial: PREenchidas})
        expect(screen.getByText('Esquadria 1')).toBeInTheDocument()
        expect(screen.getByText('Esquadria 2')).toBeInTheDocument()
        // 2 cards → múltiplos labels iguais; consulta por índice do card
        expect(screen.getAllByLabelText('Peitoril (m)')[1]).toHaveValue(1.1)
        expect(screen.getAllByLabelText('Info adicional (opcional)')[0]).toHaveValue('Porta dupla')
    })

    it('adiciona e remove itens (useFieldArray)', () => {
        renderModal()

        fireEvent.click(screen.getByRole('button', {name: 'Adicionar esquadria'}))
        expect(screen.getByText('Esquadria 2')).toBeInTheDocument()

        fireEvent.click(screen.getByLabelText('Remover esquadria 2'))
        expect(screen.queryByText('Esquadria 2')).not.toBeInTheDocument()
    })

    it('não submete com base vazia (positive) e exibe erro inline', async () => {
        const {onSubmit} = renderModal()

        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            expect(screen.getAllByRole('alert').length).toBeGreaterThan(0)
        })
        expect(onSubmit).not.toHaveBeenCalled()
    })

    it('submit válido entrega a lista ao onSubmit, toasta e chama onSalvou', async () => {
        const {onSubmit, onSalvou, onOpenChange} = renderModal()

        fireEvent.change(screen.getByLabelText('Base (m)'), {target: {value: '0.9'}})
        fireEvent.change(screen.getByLabelText('Altura (m)'), {target: {value: '2.1'}})
        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            expect(onSubmit).toHaveBeenCalledWith([
                {
                    tipo: 'PORTA',
                    geometria: {base: 0.9, altura: 2.1, repeticao: 1},
                    material: 'NAO_SE_APLICA',
                    alturaPeitoril: 0,
                    informacaoAdicional: '',
                },
            ])
        })
        const {toast} = await import('sonner')
        expect(toast.success).toHaveBeenCalledWith('Esquadrias incluídas.')
        expect(onSalvou).toHaveBeenCalledTimes(1)
        expect(onOpenChange).toHaveBeenCalledWith(false)
    })

    it('modo editar usa a mensagem de sucesso de atualização', async () => {
        const {onSubmit} = renderModal({
            modo: 'editar',
            titulo: 'Editar Esquadrias',
            inicial: PREenchidas,
        })

        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            expect(onSubmit).toHaveBeenCalledWith(PREenchidas)
        })
        const {toast} = await import('sonner')
        expect(toast.success).toHaveBeenCalledWith('Esquadrias atualizadas.')
    })

    it('erro do backend mantém o modal aberto', async () => {
        const {onSubmit, onOpenChange} = renderModal()
        onSubmit.mockRejectedValueOnce(
            Object.assign(new Error('Request failed'), {
                isAxiosError: true,
                response: {status: 400, data: {mensagem: 'Esquadria inválida.'}},
            }),
        )

        fireEvent.change(screen.getByLabelText('Base (m)'), {target: {value: '0.9'}})
        fireEvent.change(screen.getByLabelText('Altura (m)'), {target: {value: '2.1'}})
        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        const {toast} = await import('sonner')
        await waitFor(() => {
            expect(toast.error).toHaveBeenCalledWith('Esquadria inválida.')
        })
        expect(onOpenChange).not.toHaveBeenCalled()
        expect(screen.getByText('Incluir Esquadrias')).toBeInTheDocument()
    })

    it('cancelar fecha sem chamar onSubmit', () => {
        const {onSubmit, onOpenChange} = renderModal()

        fireEvent.click(screen.getByRole('button', {name: 'Cancelar'}))

        expect(onOpenChange).toHaveBeenCalledWith(false)
        expect(onSubmit).not.toHaveBeenCalled()
    })
})
