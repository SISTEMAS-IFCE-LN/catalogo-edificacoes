import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ModalConfirmacao } from './ModalConfirmacao'

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
  },
}))

describe('ModalConfirmacao', () => {
  const mockOnConfirm = vi.fn()
  const mockOnOpenChange = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renderiza título quando aberto', () => {
    render(
      <ModalConfirmacao
        open={true}
        title="Confirmar ação"
        onConfirm={mockOnConfirm}
        onOpenChange={mockOnOpenChange}
      />
    )
    expect(screen.getByText('Confirmar ação')).toBeInTheDocument()
  })

  it('renderiza descrição quando fornecida', () => {
    render(
      <ModalConfirmacao
        open={true}
        title="Confirmar ação"
        description="Esta ação não pode ser desfeita."
        onConfirm={mockOnConfirm}
        onOpenChange={mockOnOpenChange}
      />
    )
    expect(screen.getByText('Esta ação não pode ser desfeita.')).toBeInTheDocument()
  })

  it('não renderiza descrição quando não fornecida', () => {
    render(
      <ModalConfirmacao
        open={true}
        title="Confirmar ação"
        onConfirm={mockOnConfirm}
        onOpenChange={mockOnOpenChange}
      />
    )
    expect(screen.queryByText('Esta ação não pode ser desfeita.')).not.toBeInTheDocument()
  })

  it('renderiza botão Cancelar', () => {
    render(
      <ModalConfirmacao
        open={true}
        title="Confirmar ação"
        onConfirm={mockOnConfirm}
        onOpenChange={mockOnOpenChange}
      />
    )
    expect(screen.getByText('Cancelar')).toBeInTheDocument()
  })

  it('renderiza botão Confirmar com label padrão', () => {
    render(
      <ModalConfirmacao
        open={true}
        title="Confirmar ação"
        onConfirm={mockOnConfirm}
        onOpenChange={mockOnOpenChange}
      />
    )
    expect(screen.getByText('Confirmar')).toBeInTheDocument()
  })

  it('renderiza botão Confirmar com label customizado', () => {
    render(
      <ModalConfirmacao
        open={true}
        title="Confirmar ação"
        onConfirm={mockOnConfirm}
        onOpenChange={mockOnOpenChange}
        confirmLabel="Excluir"
      />
    )
    expect(screen.getByText('Excluir')).toBeInTheDocument()
  })

  it('chama onOpenChange(false) ao clicar em Cancelar', () => {
    render(
      <ModalConfirmacao
        open={true}
        title="Confirmar ação"
        onConfirm={mockOnConfirm}
        onOpenChange={mockOnOpenChange}
      />
    )
    fireEvent.click(screen.getByText('Cancelar'))
    expect(mockOnOpenChange).toHaveBeenCalledWith(false)
  })

  it('chama onConfirm ao clicar em Confirmar', async () => {
    mockOnConfirm.mockResolvedValueOnce(undefined)
    render(
      <ModalConfirmacao
        open={true}
        title="Confirmar ação"
        onConfirm={mockOnConfirm}
        onOpenChange={mockOnOpenChange}
      />
    )
    fireEvent.click(screen.getByText('Confirmar'))
    await waitFor(() => expect(mockOnConfirm).toHaveBeenCalled())
  })

  it('chama onOpenChange(false) após onConfirm bem-sucedido', async () => {
    mockOnConfirm.mockResolvedValueOnce(undefined)
    render(
      <ModalConfirmacao
        open={true}
        title="Confirmar ação"
        onConfirm={mockOnConfirm}
        onOpenChange={mockOnOpenChange}
      />
    )
    fireEvent.click(screen.getByText('Confirmar'))
    await waitFor(() => expect(mockOnOpenChange).toHaveBeenCalledWith(false))
  })

  it('desabilita botão durante execução', async () => {
    let resolvePromise: () => void
    const promise = new Promise<void>((resolve) => {
      resolvePromise = resolve
    })
    mockOnConfirm.mockReturnValueOnce(promise)

    render(
      <ModalConfirmacao
        open={true}
        title="Confirmar ação"
        onConfirm={mockOnConfirm}
        onOpenChange={mockOnOpenChange}
      />
    )

    fireEvent.click(screen.getByText('Confirmar'))

    await waitFor(() => {
      expect(screen.getByText('Executando…')).toBeInTheDocument()
      expect(screen.getByText('Executando…')).toBeDisabled()
    })

    resolvePromise!()
    await waitFor(() => {
      expect(screen.getByText('Confirmar')).not.toBeDisabled()
    })
  })

  it('exibe toast de erro quando onConfirm falha', async () => {
    const error = new Error('Erro')
    mockOnConfirm.mockRejectedValueOnce(error)

    render(
      <ModalConfirmacao
        open={true}
        title="Confirmar ação"
        onConfirm={mockOnConfirm}
        onOpenChange={mockOnOpenChange}
      />
    )

    fireEvent.click(screen.getByText('Confirmar'))

    await waitFor(() => {
      expect(screen.getByText('Confirmar')).not.toBeDisabled()
    })

    // Verifica se o toast de erro foi chamado
    const { toast } = await import('sonner')
    expect(toast.error).toHaveBeenCalledWith('Erro ao executar ação. Tente novamente.')
  })

  it('aplica variant destructive ao botão', () => {
    render(
      <ModalConfirmacao
        open={true}
        title="Confirmar exclusão"
        onConfirm={mockOnConfirm}
        onOpenChange={mockOnOpenChange}
        variant="destructive"
      />
    )
    const button = screen.getByText('Confirmar')
    // O shadcn/ui aplica classes como 'text-destructive' e 'bg-destructive/10' para variant destructive
    expect(button.className).toMatch(/destructive/)
  })
})
