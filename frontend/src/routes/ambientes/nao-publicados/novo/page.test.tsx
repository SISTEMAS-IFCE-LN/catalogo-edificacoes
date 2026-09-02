import { render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { NovoAmbientePage } from './page'
import type { AmbienteInput } from '@/types/ambientes/request'
import { toast } from 'sonner'

const { mockNavigate, captured } = vi.hoisted(() => ({
  mockNavigate: vi.fn(),
  captured: {
    submit: null as ((values: AmbienteInput) => Promise<void>) | null,
  },
}))

vi.mock('react-router', () => ({ useNavigate: () => mockNavigate }))

vi.mock('@/components/ambientes/FormAmbiente', () => ({
  FormAmbiente: ({ onSubmit }: { onSubmit: (values: AmbienteInput) => Promise<void> }) => {
    captured.submit = onSubmit
    return <div data-testid="form-ambiente-mock" />
  },
}))

const mockCriarAmbiente = vi.hoisted(() => vi.fn())
vi.mock('@/lib/api/api-naopublicados', () => ({
  criarAmbiente: (payload: AmbienteInput) => mockCriarAmbiente(payload),
}))

vi.mock('sonner', () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}))

const AMBIENTE_VALIDO: AmbienteInput = {
  nome: 'Sala 01',
  localizacao: { bloco: 'BLOCO_1', unidade: 'SEDE', andar: 0 },
  tipo: 'SALA_AULA',
  capacidade: 1,
  geometrias: [{ tipo: 'RETANGULAR', base: 4, altura: 3, repeticao: 1 }],
  pesDireitos: [2.8],
  esquadrias: [{
    tipo: 'PORTA',
    geometria: { base: 0.9, altura: 2.1, repeticao: 1 },
    material: 'ALUMINIO',
    alturaPeitoril: 0,
    informacaoAdicional: '',
  }],
  informacaoAdicional: 'Sala com ar-condicionado',
}

describe('NovoAmbientePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    captured.submit = null
  })

  it('cria o ambiente e navega para o detalhe', async () => {
    mockCriarAmbiente.mockResolvedValue({ id: 42 })
    render(<NovoAmbientePage />)
    expect(screen.getByText('Novo Ambiente')).toBeInTheDocument()

    await captured.submit!(AMBIENTE_VALIDO)

    await waitFor(() => expect(mockCriarAmbiente).toHaveBeenCalledWith(AMBIENTE_VALIDO))
    expect(toast.success).toHaveBeenCalledWith('Ambiente criado.')
    expect(mockNavigate).toHaveBeenCalledWith('/ambientes/nao-publicados/42')
  })

  it('exibe mensagem do backend e não navega em erro de criação', async () => {
    mockCriarAmbiente.mockRejectedValue(new Error('boom'))
    render(<NovoAmbientePage />)

    await captured.submit!(AMBIENTE_VALIDO)

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith('Erro ao criar ambiente.'))
    expect(mockNavigate).not.toHaveBeenCalled()
  })
})
