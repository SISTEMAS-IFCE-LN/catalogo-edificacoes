import {fireEvent, render, screen, waitFor} from '@testing-library/react'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {ModalPesDireitos} from './ModalPesDireitos'

vi.mock('sonner', () => ({
    toast: {success: vi.fn(), error: vi.fn()},
}))

type PropsDoComponente = Parameters<typeof ModalPesDireitos>[0]

function renderModal(props: Partial<PropsDoComponente> = {}) {
    const onSubmit = vi.fn<PropsDoComponente['onSubmit']>().mockResolvedValue(undefined)
    const onSalvou = vi.fn()
    const onOpenChange = vi.fn()

    render(
        <ModalPesDireitos
            open
            modo="incluir"
            titulo="Incluir Pés-direitos"
            inicial={[]}
            onOpenChange={onOpenChange}
            onSubmit={onSubmit}
            onSalvou={onSalvou}
            {...props}
        />,
    )

    return {onSubmit, onSalvou, onOpenChange}
}

describe('ModalPesDireitos (UC10/UC11-FE)', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('modo incluir inicia com 1 campo em branco', () => {
        renderModal()
        expect(screen.getByLabelText('Pé-direito 1 (m)')).toBeInTheDocument()
        expect(screen.queryByLabelText('Pé-direito 2 (m)')).not.toBeInTheDocument()
    })

    it('modo editar pré-preenche as alturas recebidas', () => {
        renderModal({modo: 'editar', titulo: 'Editar Pés-direitos', inicial: [2.8, 3.2]})
        expect(screen.getByLabelText('Pé-direito 1 (m)')).toHaveValue(2.8)
        expect(screen.getByLabelText('Pé-direito 2 (m)')).toHaveValue(3.2)
    })

    it('adiciona e remove alturas', () => {
        renderModal()

        fireEvent.click(screen.getByRole('button', {name: 'Adicionar pé-direito'}))
        expect(screen.getByLabelText('Pé-direito 2 (m)')).toBeInTheDocument()

        fireEvent.click(screen.getByLabelText('Remover pé-direito 2'))
        expect(screen.queryByLabelText('Pé-direito 2 (m)')).not.toBeInTheDocument()
    })

    it('não submete com valor 0 (positive > 0) e exibe erro inline', async () => {
        const {onSubmit} = renderModal()

        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            expect(screen.getByRole('alert')).toBeInTheDocument()
        })
        expect(onSubmit).not.toHaveBeenCalled()
    })

    it('submit válido entrega as alturas, toasta e chama onSalvou', async () => {
        const {onSubmit, onSalvou, onOpenChange} = renderModal()

        fireEvent.change(screen.getByLabelText('Pé-direito 1 (m)'), {target: {value: '2.8'}})
        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            expect(onSubmit).toHaveBeenCalledWith([2.8])
        })
        const {toast} = await import('sonner')
        expect(toast.success).toHaveBeenCalledWith('Pés-direitos incluídos.')
        expect(onSalvou).toHaveBeenCalledTimes(1)
        expect(onOpenChange).toHaveBeenCalledWith(false)
    })

    it('modo editar usa a mensagem de sucesso de atualização', async () => {
        const {onSubmit} = renderModal({modo: 'editar', titulo: 'Editar Pés-direitos', inicial: [3]})

        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            expect(onSubmit).toHaveBeenCalledWith([3])
        })
        const {toast} = await import('sonner')
        expect(toast.success).toHaveBeenCalledWith('Pés-direitos atualizados.')
    })

    it('erro do backend mantém o modal aberto', async () => {
        const {onSubmit, onOpenChange} = renderModal()
        onSubmit.mockRejectedValueOnce(
            Object.assign(new Error('Request failed'), {
                isAxiosError: true,
                response: {status: 400, data: {mensagem: 'Pé-direito inválido.'}},
            }),
        )

        fireEvent.change(screen.getByLabelText('Pé-direito 1 (m)'), {target: {value: '2.8'}})
        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        const {toast} = await import('sonner')
        await waitFor(() => {
            expect(toast.error).toHaveBeenCalledWith('Pé-direito inválido.')
        })
        expect(onOpenChange).not.toHaveBeenCalled()
        expect(screen.getByText('Incluir Pés-direitos')).toBeInTheDocument()
    })

    it('cancelar fecha sem chamar onSubmit', () => {
        const {onSubmit, onOpenChange} = renderModal()

        fireEvent.click(screen.getByRole('button', {name: 'Cancelar'}))

        expect(onOpenChange).toHaveBeenCalledWith(false)
        expect(onSubmit).not.toHaveBeenCalled()
    })
})
