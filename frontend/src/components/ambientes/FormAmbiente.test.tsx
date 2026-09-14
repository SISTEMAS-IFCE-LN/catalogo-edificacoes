import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { FormAmbiente } from './FormAmbiente'

// O texto da etapa é dividido entre nós de texto e um <strong> — o matcher por
// função compara o textContent do wrapper (div), não um nó de texto único.
function buscarEtapa(n: number, nome: string) {
  const alvo = `Etapa ${n} de 5: ${nome}`
  return screen.queryByText((_, el) => el?.tagName === 'DIV' && el.textContent === alvo)
}

describe('FormAmbiente', () => {
  const onSubmit = vi.fn().mockResolvedValue(undefined)

  beforeEach(() => {
    vi.clearAllMocks()
  })

  async function avancar(n: number, nome: string) {
    fireEvent.click(screen.getByRole('button', { name: 'Próximo' }))
    await waitFor(() => expect(buscarEtapa(n, nome)).not.toBeNull())
  }

  /** Preenche as etapas 1–4 com dados válidos e para na 5ª (Informação Adicional). */
  async function ateAInfoAdicional() {
    fireEvent.change(screen.getByLabelText('Nome'), { target: { value: 'Sala 01' } })
    await avancar(2, 'Geometrias')
    fireEvent.change(screen.getByLabelText('Base (m)'), { target: { value: '4' } })
    fireEvent.change(screen.getByLabelText('Altura (m)'), { target: { value: '3' } })
    await avancar(3, 'Pés-direitos')
    fireEvent.change(screen.getByLabelText('Pé-direito 1 (m)'), { target: { value: '2.8' } })
    await avancar(4, 'Esquadrias')
    fireEvent.change(screen.getByLabelText('Base (m)'), { target: { value: '0.9' } })
    fireEvent.change(screen.getByLabelText('Altura (m)'), { target: { value: '2.1' } })
    await avancar(5, 'Informação Adicional')
  }

  it('inicia na etapa 1 de 5', () => {
    render(<FormAmbiente onSubmit={onSubmit} />)
    expect(buscarEtapa(1, 'Dados Básicos')).not.toBeNull()
  })

  it('não avança com a etapa atual inválida', async () => {
    render(<FormAmbiente onSubmit={onSubmit} />)
    fireEvent.click(screen.getByRole('button', { name: 'Próximo' }))
    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument())
    expect(buscarEtapa(2, 'Geometrias')).toBeNull()
  })

  it('avança etapa a etapa até a 5ª (Informação Adicional)', async () => {
    render(<FormAmbiente onSubmit={onSubmit} />)
    await ateAInfoAdicional()
    expect(screen.getByLabelText('Informação Adicional (opcional)')).toBeInTheDocument()
  })

  it('salva com informação adicional vazia (campo opcional)', async () => {
    render(<FormAmbiente onSubmit={onSubmit} />)
    await ateAInfoAdicional()
    fireEvent.click(screen.getByRole('button', { name: 'Salvar' }))
    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1))
    expect(onSubmit.mock.calls[0][0].informacaoAdicional).toBe('')
  })

  it('salva com informação adicional preenchida', async () => {
    render(<FormAmbiente onSubmit={onSubmit} />)
    await ateAInfoAdicional()
    fireEvent.change(screen.getByLabelText('Informação Adicional (opcional)'), {
      target: { value: 'Sala com ar-condicionado' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Salvar' }))
    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1))
    expect(onSubmit.mock.calls[0][0].informacaoAdicional).toBe('Sala com ar-condicionado')
  })

  it('bloqueia o submit quando a informação adicional excede 255 caracteres', async () => {
    render(<FormAmbiente onSubmit={onSubmit} />)
    await ateAInfoAdicional()
    fireEvent.change(screen.getByLabelText('Informação Adicional (opcional)'), {
      target: { value: 'a'.repeat(256) },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Salvar' }))
    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument())
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('adiciona e remove geometrias (useFieldArray)', async () => {
    render(<FormAmbiente onSubmit={onSubmit} />)
    fireEvent.change(screen.getByLabelText('Nome'), { target: { value: 'Sala 01' } })
    await avancar(2, 'Geometrias')

    fireEvent.click(screen.getByRole('button', { name: 'Adicionar geometria' }))
    expect(screen.getByText('Geometria 2')).toBeInTheDocument()

    fireEvent.click(screen.getByLabelText('Remover geometria 2'))
    expect(screen.queryByText('Geometria 2')).not.toBeInTheDocument()
  })

  it('adiciona e remove pés-direitos', async () => {
    render(<FormAmbiente onSubmit={onSubmit} />)
    fireEvent.change(screen.getByLabelText('Nome'), { target: { value: 'Sala 01' } })
    await avancar(2, 'Geometrias')
    fireEvent.change(screen.getByLabelText('Base (m)'), { target: { value: '4' } })
    fireEvent.change(screen.getByLabelText('Altura (m)'), { target: { value: '3' } })
    await avancar(3, 'Pés-direitos')

    fireEvent.click(screen.getByRole('button', { name: 'Adicionar pé-direito' }))
    expect(screen.getByLabelText('Pé-direito 2 (m)')).toBeInTheDocument()

    fireEvent.click(screen.getByLabelText('Remover pé-direito 2'))
    expect(screen.queryByLabelText('Pé-direito 2 (m)')).not.toBeInTheDocument()
  })
})
