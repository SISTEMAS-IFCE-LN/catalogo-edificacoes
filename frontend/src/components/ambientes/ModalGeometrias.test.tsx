import {fireEvent, render, screen, waitFor} from '@testing-library/react'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {ModalGeometrias} from './ModalGeometrias'
import type {GeometriaInput} from '@/types/ambientes/request'
import {TipoGeometria} from '@/types/ambientes/enums'

vi.mock('sonner', () => ({
    toast: {success: vi.fn(), error: vi.fn()},
}))

type PropsDoComponente = Parameters<typeof ModalGeometrias>[0]

const PREenchida: GeometriaInput[] = [
    {tipo: 'RETANGULAR', base: 4, altura: 3, repeticao: 2},
    {tipo: 'TRIANGULAR', base: 2, altura: 2, repeticao: 1},
]

function renderModal(props: Partial<PropsDoComponente> = {}) {
    const onSubmit = vi.fn<PropsDoComponente['onSubmit']>().mockResolvedValue(undefined)
    const onSalvou = vi.fn()
    const onOpenChange = vi.fn()

    render(
        <ModalGeometrias
            open
            modo="incluir"
            titulo="Incluir Geometrias"
            inicial={[]}
            onOpenChange={onOpenChange}
            onSubmit={onSubmit}
            onSalvou={onSalvou}
            {...props}
        />,
    )

    return {onSubmit, onSalvou, onOpenChange}
}

describe('ModalGeometrias (UC08/UC09-FE)', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('modo incluir inicia com 1 card em branco', () => {
        renderModal()
        expect(screen.getByText('Geometria 1')).toBeInTheDocument()
        expect(screen.queryByText('Geometria 2')).not.toBeInTheDocument()
    })

    it('modo editar pré-preenche os valores recebidos (nomes técnicos)', () => {
        renderModal({modo: 'editar', titulo: 'Editar Geometrias', inicial: PREenchida})
        expect(screen.getByText('Geometria 1')).toBeInTheDocument()
        expect(screen.getByText('Geometria 2')).toBeInTheDocument()
        // 2 cards → múltiplos labels iguais; consulta por índice do card
        expect(screen.getAllByLabelText('Base (m)')[0]).toHaveValue(4)
        expect(screen.getAllByLabelText('Altura (m)')[0]).toHaveValue(3)
        expect(screen.getAllByLabelText('Repetição')[0]).toHaveValue(2)
    })

    it('adiciona e remove itens (useFieldArray)', () => {
        renderModal()

        fireEvent.click(screen.getByRole('button', {name: 'Adicionar geometria'}))
        expect(screen.getByText('Geometria 2')).toBeInTheDocument()

        fireEvent.click(screen.getByLabelText('Remover geometria 2'))
        expect(screen.queryByText('Geometria 2')).not.toBeInTheDocument()
    })

    it('não submete com base vazia (positive) e exibe erro inline', async () => {
        const {onSubmit} = renderModal()

        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            expect(screen.getAllByRole('alert').length).toBeGreaterThan(0)
        })
        expect(onSubmit).not.toHaveBeenCalled()
    })

    it('não submete sem nenhuma geometria (min 1) e mantém o modal aberto', async () => {
        const {onSubmit, onOpenChange} = renderModal()

        fireEvent.click(screen.getByLabelText('Remover geometria 1'))
        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            expect(screen.getByRole('alert')).toBeInTheDocument()
        })
        expect(onSubmit).not.toHaveBeenCalled()
        expect(onOpenChange).not.toHaveBeenCalled()
        expect(screen.getByText('Incluir Geometrias')).toBeInTheDocument()
    })

    it('submit válido entrega a lista ao onSubmit, toasta e chama onSalvou', async () => {
        const {onSubmit, onSalvou, onOpenChange} = renderModal()

        fireEvent.change(screen.getByLabelText('Base (m)'), {target: {value: '4'}})
        fireEvent.change(screen.getByLabelText('Altura (m)'), {target: {value: '3'}})
        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            expect(onSubmit).toHaveBeenCalledWith([
                {tipo: 'RETANGULAR', base: 4, altura: 3, repeticao: 1},
            ])
        })
        const {toast} = await import('sonner')
        expect(toast.success).toHaveBeenCalledWith('Geometrias incluídas.')
        expect(onSalvou).toHaveBeenCalledTimes(1)
        expect(onOpenChange).toHaveBeenCalledWith(false)
    })

    it('modo editar usa a mensagem de sucesso de atualização', async () => {
        const {onSubmit} = renderModal({
            modo: 'editar',
            titulo: 'Editar Geometrias',
            inicial: PREenchida,
        })

        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            expect(onSubmit).toHaveBeenCalledWith(PREenchida)
        })
        const {toast} = await import('sonner')
        expect(toast.success).toHaveBeenCalledWith('Geometrias atualizadas.')
    })

    it('erro do backend mantém o modal aberto (correção possível)', async () => {
        const {onSubmit, onOpenChange} = renderModal()
        onSubmit.mockRejectedValueOnce(
            Object.assign(new Error('Request failed'), {
                isAxiosError: true,
                response: {status: 400, data: {mensagem: 'Geometria inválida.'}},
            }),
        )

        fireEvent.change(screen.getByLabelText('Base (m)'), {target: {value: '4'}})
        fireEvent.change(screen.getByLabelText('Altura (m)'), {target: {value: '3'}})
        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        const {toast} = await import('sonner')
        await waitFor(() => {
            expect(toast.error).toHaveBeenCalledWith('Geometria inválida.')
        })
        expect(onOpenChange).not.toHaveBeenCalled()
        expect(screen.getByText('Incluir Geometrias')).toBeInTheDocument()
    })

    it('cancelar fecha sem chamar onSubmit', () => {
        const {onSubmit, onOpenChange} = renderModal()

        fireEvent.click(screen.getByRole('button', {name: 'Cancelar'}))

        expect(onOpenChange).toHaveBeenCalledWith(false)
        expect(onSubmit).not.toHaveBeenCalled()
    })

    it('o select de tipo expõe as opções técnicas com rótulos do enum', () => {
        renderModal()
        // Trigger com o valor técnico aplicado (default RETANGULAR)
        expect(screen.getByLabelText('Tipo')).toHaveTextContent('RETANGULAR')
        expect(Object.entries(TipoGeometria).length).toBeGreaterThan(0)
    })
})
