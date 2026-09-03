import {fireEvent, render, screen, waitFor} from '@testing-library/react'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {ModalFormulario, type ModalFormularioProps} from './ModalFormulario'
import {Input} from '@/components/ui/input'
import {Label} from '@/components/ui/label'
import {z} from 'zod'

vi.mock('sonner', () => ({
    toast: {success: vi.fn(), error: vi.fn()},
}))

// Schema de teste com transform (trim) para validar que o shell entrega ao
// onSubmit os valores PARSEADOS (output do zod), não o input cru.
const testeSchema = z.object({
    nome: z.string().trim().min(1, 'Nome obrigatório.'),
})
type ValoresTeste = z.infer<typeof testeSchema>
type PropsDoShell = ModalFormularioProps<ValoresTeste, number>

function renderShell(overrides: Partial<PropsDoShell> = {}) {
    const onSubmit = vi.fn<PropsDoShell['onSubmit']>().mockResolvedValue(42)
    const onSalvou = vi.fn<PropsDoShell['onSalvou']>()
    const onOpenChange = vi.fn<PropsDoShell['onOpenChange']>()

    render(
        <ModalFormulario<ValoresTeste, number>
            open
            title="Teste Shell"
            description="Descrição do shell"
            schema={testeSchema}
            defaults={{nome: 'Maria'}}
            onOpenChange={onOpenChange}
            onSubmit={onSubmit}
            onSalvou={onSalvou}
            {...overrides}
        >
            {(form) => (
                <div className="space-y-1.5">
                    <Label htmlFor="shell-nome">Nome</Label>
                    <Input id="shell-nome" {...form.register('nome')}/>
                    {form.formState.errors.nome && (
                        <p role="alert" className="text-sm text-destructive">
                            {form.formState.errors.nome.message}
                        </p>
                    )}
                </div>
            )}
        </ModalFormulario>,
    )

    return {onSubmit, onSalvou, onOpenChange}
}

function erroDoBackend(mensagem: string): Error {
    return Object.assign(new Error('Request failed'), {
        isAxiosError: true,
        response: {status: 409, data: {mensagem}},
    })
}

describe('ModalFormulario (shell)', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('renderiza título, descrição e os valores iniciais (defaults)', () => {
        renderShell()
        expect(screen.getByText('Teste Shell')).toBeInTheDocument()
        expect(screen.getByText('Descrição do shell')).toBeInTheDocument()
        expect(screen.getByLabelText('Nome')).toHaveValue('Maria')
    })

    it('submit válido: entrega valores parseados, toasta, chama onSalvou com o retorno e fecha', async () => {
        const {onSubmit, onSalvou, onOpenChange} = renderShell()

        fireEvent.change(screen.getByLabelText('Nome'), {target: {value: '  Ana  '}})
        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            // Output do parse (trim aplicado), não o input cru
            expect(onSubmit).toHaveBeenCalledWith({nome: 'Ana'})
        })
        const {toast} = await import('sonner')
        expect(toast.success).toHaveBeenCalledWith('Salvo com sucesso.')
        // R = number: o retorno do onSubmit chega ao onSalvou
        expect(onSalvou).toHaveBeenCalledWith(42)
        expect(onOpenChange).toHaveBeenCalledWith(false)
    })

    it('submit inválido: exibe erro inline, não chama onSubmit nem fecha', async () => {
        const {onSubmit, onOpenChange} = renderShell()

        fireEvent.change(screen.getByLabelText('Nome'), {target: {value: '   '}})
        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            expect(screen.getByRole('alert')).toHaveTextContent('Nome obrigatório.')
        })
        expect(onSubmit).not.toHaveBeenCalled()
        expect(onOpenChange).not.toHaveBeenCalled()
    })

    it('erro do backend: toast com ErroRes.mensagem e modal permanece aberto', async () => {
        const {onSubmit, onOpenChange} = renderShell()
        onSubmit.mockRejectedValueOnce(erroDoBackend('Falha de negócio.'))

        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        const {toast} = await import('sonner')
        await waitFor(() => {
            expect(toast.error).toHaveBeenCalledWith('Falha de negócio.')
        })
        expect(onOpenChange).not.toHaveBeenCalled()
        expect(screen.getByText('Teste Shell')).toBeInTheDocument()
    })

    it('erro sem ErroRes.mensagem: toast com mensagemPadrao', async () => {
        const {onSubmit} = renderShell({mensagemPadrao: 'Erro ao testar.'})
        onSubmit.mockRejectedValueOnce(new Error('rede caiu'))

        fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        const {toast} = await import('sonner')
        await waitFor(() => {
            expect(toast.error).toHaveBeenCalledWith('Erro ao testar.')
        })
    })

    it('cancelar: apenas fecha, sem chamar onSubmit', () => {
        const {onSubmit, onOpenChange} = renderShell()

        fireEvent.click(screen.getByRole('button', {name: 'Cancelar'}))

        expect(onOpenChange).toHaveBeenCalledWith(false)
        expect(onSubmit).not.toHaveBeenCalled()
    })

    it('salvarLabel customizado e estado de salvando desabilita o botão', async () => {
        const {onSubmit} = renderShell({salvarLabel: 'Duplicar'})

        expect(screen.getByRole('button', {name: 'Duplicar'})).toBeInTheDocument()

        onSubmit.mockReturnValueOnce(new Promise(() => {
        }))
        fireEvent.click(screen.getByRole('button', {name: 'Duplicar'}))

        await waitFor(() => {
            expect(screen.getByRole('button', {name: 'Salvando…'})).toBeDisabled()
        })
    })
})
