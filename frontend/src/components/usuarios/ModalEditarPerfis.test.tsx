import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ModalEditarPerfis } from './ModalEditarPerfis'
import { Role, type User } from '@/types/usuarios/user'
import { toast } from 'sonner'

vi.mock('sonner', () => ({
    toast: { error: vi.fn() },
}))

function makeUser(overrides: Partial<User> = {}): User {
    return {
        id: 1,
        email: 'usuario.a@ifce.edu.br',
        nome: 'Usuário A',
        ativo: true,
        criadoEm: '2025-01-01T00:00:00.000Z',
        perfis: [Role.COLABORADOR],
        ...overrides,
    }
}

// O Checkbox do Base UI renderiza um <span role="checkbox"> visível e um
// <input> oculto (aria-hidden). Consultar por role evita o match duplo.
function checkboxPorRole(label: string) {
    return screen.getByRole('checkbox', {name: label})
}

describe('ModalEditarPerfis', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('marca os perfis do usuário ao abrir', () => {
        const usuario = makeUser({perfis: [Role.COLABORADOR, Role.ADMINISTRADOR]})
        render(<ModalEditarPerfis open usuario={usuario} onOpenChange={vi.fn()} onSalvar={vi.fn()}/>)

        expect(checkboxPorRole('Administrador')).toBeChecked()
        expect(checkboxPorRole('Colaborador')).toBeChecked()
        expect(checkboxPorRole('Validador')).not.toBeChecked()
    })

    it('mantém COLABORADOR marcado e desabilitado', () => {
        const usuario = makeUser({perfis: [Role.COLABORADOR, Role.ADMINISTRADOR]})
        render(<ModalEditarPerfis open usuario={usuario} onOpenChange={vi.fn()} onSalvar={vi.fn()}/>)

        expect(checkboxPorRole('Colaborador')).toBeChecked()
        // O Base UI expressa o estado desabilitado via aria-disabled (não em <input disabled>).
        expect(checkboxPorRole('Colaborador')).toHaveAttribute('aria-disabled', 'true')
        expect(checkboxPorRole('Administrador')).not.toHaveAttribute('aria-disabled', 'true')
    })

    it('não vaza perfis de um usuário para outro ao reabrir (regressão A1)', () => {
        const onOpenChange = vi.fn()
        const onSalvar = vi.fn()
        const usuarioA = makeUser({id: 1, nome: 'Usuário A', perfis: [Role.COLABORADOR, Role.ADMINISTRADOR]})
        const usuarioB = makeUser({id: 2, nome: 'Usuário B', perfis: [Role.COLABORADOR]})

        const {rerender} = render(
            <ModalEditarPerfis open usuario={usuarioA} onOpenChange={onOpenChange} onSalvar={onSalvar}/>,
        )
        expect(checkboxPorRole('Administrador')).toBeChecked()

        // Fecha sem salvar.
        rerender(<ModalEditarPerfis open={false} usuario={usuarioA} onOpenChange={onOpenChange} onSalvar={onSalvar}/>)

        // Reabre para um usuário com menos perfis.
        rerender(<ModalEditarPerfis open usuario={usuarioB} onOpenChange={onOpenChange} onSalvar={onSalvar}/>)

        expect(checkboxPorRole('Administrador')).not.toBeChecked()
        expect(checkboxPorRole('Colaborador')).toBeChecked()
    })

    it('salva incluindo COLABORADOR e fecha no sucesso', async () => {
        const onSalvar = vi.fn().mockResolvedValue(undefined)
        const onOpenChange = vi.fn()
        const usuario = makeUser({perfis: [Role.COLABORADOR, Role.ADMINISTRADOR]})

        render(<ModalEditarPerfis open usuario={usuario} onOpenChange={onOpenChange} onSalvar={onSalvar}/>)

        await userEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            expect(onSalvar).toHaveBeenCalledWith(1, [Role.COLABORADOR, Role.ADMINISTRADOR])
            expect(onOpenChange).toHaveBeenCalledWith(false)
        })
    })

    it('exibe toast de erro quando salvar falha', async () => {
        const onSalvar = vi.fn().mockRejectedValue({
            isAxiosError: true,
            response: {status: 409, data: {mensagem: 'Último administrador não pode ser alterado.'}},
        })
        const usuario = makeUser()

        render(<ModalEditarPerfis open usuario={usuario} onOpenChange={vi.fn()} onSalvar={onSalvar}/>)

        await userEvent.click(screen.getByRole('button', {name: 'Salvar'}))

        await waitFor(() => {
            expect(toast.error).toHaveBeenCalledWith('Último administrador não pode ser alterado.')
        })
    })
})
