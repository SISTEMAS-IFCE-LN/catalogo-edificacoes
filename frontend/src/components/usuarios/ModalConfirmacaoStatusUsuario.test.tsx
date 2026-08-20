import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ModalConfirmacaoStatusUsuario } from './ModalConfirmacaoStatusUsuario'
import { Role, type User } from '@/types/usuarios/user'

vi.mock('sonner', () => ({
    toast: { error: vi.fn() },
}))

const usuario: User = {
    id: 1,
    email: 'usuario@ifce.edu.br',
    nome: 'Usuário Teste',
    ativo: true,
    criadoEm: '2025-01-01T00:00:00.000Z',
    perfis: [Role.COLABORADOR],
}

describe('ModalConfirmacaoStatusUsuario', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('renderiza título de desativação quando acao=desativar', () => {
        render(
            <ModalConfirmacaoStatusUsuario
                open
                usuario={usuario}
                acao="desativar"
                onConfirmar={vi.fn()}
                onOpenChange={vi.fn()}
            />,
        )
        expect(screen.getByText('Desativar Usuário Teste?')).toBeInTheDocument()
    })

    it('renderiza título de ativação quando acao=ativar', () => {
        render(
            <ModalConfirmacaoStatusUsuario
                open
                usuario={usuario}
                acao="ativar"
                onConfirmar={vi.fn()}
                onOpenChange={vi.fn()}
            />,
        )
        expect(screen.getByText('Ativar Usuário Teste?')).toBeInTheDocument()
    })

    it('aplica variant destructive ao confirmar desativação', () => {
        render(
            <ModalConfirmacaoStatusUsuario
                open
                usuario={usuario}
                acao="desativar"
                onConfirmar={vi.fn()}
                onOpenChange={vi.fn()}
            />,
        )
        expect(screen.getByText('Confirmar').className).toMatch(/destructive/)
    })

    it('chama onConfirmar e fecha no sucesso', async () => {
        const onConfirmar = vi.fn().mockResolvedValue(undefined)
        const onOpenChange = vi.fn()
        render(
            <ModalConfirmacaoStatusUsuario
                open
                usuario={usuario}
                acao="desativar"
                onConfirmar={onConfirmar}
                onOpenChange={onOpenChange}
            />,
        )

        fireEvent.click(screen.getByText('Confirmar'))

        await waitFor(() => {
            expect(onConfirmar).toHaveBeenCalledTimes(1)
            expect(onOpenChange).toHaveBeenCalledWith(false)
        })
    })

    it('não fecha quando a confirmação falha', async () => {
        const onConfirmar = vi.fn().mockRejectedValue({isAxiosError: true, response: {status: 409, data: {mensagem: 'erro'}}})
        const onOpenChange = vi.fn()
        render(
            <ModalConfirmacaoStatusUsuario
                open
                usuario={usuario}
                acao="desativar"
                onConfirmar={onConfirmar}
                onOpenChange={onOpenChange}
            />,
        )

        fireEvent.click(screen.getByText('Confirmar'))

        await waitFor(() => {
            expect(onOpenChange).not.toHaveBeenCalled()
        })
    })
})
