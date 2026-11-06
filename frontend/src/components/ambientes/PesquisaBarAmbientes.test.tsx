import { render, screen, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import { PesquisaBarAmbientes } from './PesquisaBarAmbientes'
import { FILTROS_VAZIOS } from '@/types/ambientes/filtros'

// Referências estáveis: o filtro não precisa de QueryClient/HTTP nos testes.
vi.mock('@/hooks/useReferencias', () => {
  const campi = [
    { id: 1, nome: 'LIMOEIRO DO NORTE' },
    { id: 2, nome: 'CIDADE ALTA' },
  ]
  const unidades = [
    { id: 10, nome: 'SEDE' },
    { id: 11, nome: 'UEPE' },
  ]
  const blocos = [
    { id: 100, nome: 'BLOCO 1' },
    { id: 101, nome: 'BLOCO 2' },
  ]
  return {
    useCampus: () => ({ data: campi, isLoading: false }),
    useUnidades: (campusId: number | null) => ({
      data: campusId === null ? [] : unidades,
      isLoading: false,
    }),
    useBlocos: (unidadeId: number | null) => ({
      data: unidadeId === null ? [] : blocos,
      isLoading: false,
    }),
  }
})

const LOCALIZACAO_COMPLETA = {
  ...FILTROS_VAZIOS,
  campusId: 1,
  unidadeId: 10,
  blocoId: 100,
}

function vemAntes(a: HTMLElement, b: HTMLElement): boolean {
  return Boolean(a.compareDocumentPosition(b) & Node.DOCUMENT_POSITION_FOLLOWING)
}

describe('PesquisaBarAmbientes', () => {
  const mockOnChange = vi.fn()

  it('renderiza seletor de tipo de filtro', () => {
    render(<PesquisaBarAmbientes initial={FILTROS_VAZIOS} onChange={mockOnChange} />)
    expect(screen.getByLabelText('Tipo de filtro')).toBeInTheDocument()
  })

  it('botão Aplicar está desabilitado quando tipo=NENHUM', () => {
    render(<PesquisaBarAmbientes initial={FILTROS_VAZIOS} onChange={mockOnChange} />)

    const aplicarButton = screen.getByText('Aplicar')
    expect(aplicarButton).toBeDisabled()
  })

  it('exibe botão Limpar quando há filtros ativos', () => {
    const initial = { ...FILTROS_VAZIOS, nome: 'Sala 101' }
    render(<PesquisaBarAmbientes initial={initial} onChange={mockOnChange} />)

    expect(screen.getByText('Limpar')).toBeInTheDocument()
  })

  it('não exibe botão Limpar quando não há filtros', () => {
    render(<PesquisaBarAmbientes initial={FILTROS_VAZIOS} onChange={mockOnChange} />)

    expect(screen.queryByText('Limpar')).not.toBeInTheDocument()
  })

  it('chama onChange com filtros vazios ao clicar em Limpar', () => {
    const initial = { ...FILTROS_VAZIOS, nome: 'Sala 101' }
    render(<PesquisaBarAmbientes initial={initial} onChange={mockOnChange} />)

    fireEvent.click(screen.getByText('Limpar'))

    expect(mockOnChange).toHaveBeenCalledWith(FILTROS_VAZIOS)
  })

  it('exibe input de nome quando initial tem nome', () => {
    const initial = { ...FILTROS_VAZIOS, nome: 'Sala 101' }
    render(<PesquisaBarAmbientes initial={initial} onChange={mockOnChange} />)

    expect(screen.getByLabelText('Filtrar por nome')).toBeInTheDocument()
  })

  it('exibe select de tipo quando initial tem tipo', () => {
    const initial = { ...FILTROS_VAZIOS, tipo: 'Sala de Aula' }
    render(<PesquisaBarAmbientes initial={initial} onChange={mockOnChange} />)

    expect(screen.getByLabelText('Filtrar por tipo')).toBeInTheDocument()
  })

  it('exibe a cascata Campus → Unidade → Bloco → Andar na ordem', () => {
    render(<PesquisaBarAmbientes initial={LOCALIZACAO_COMPLETA} onChange={mockOnChange} />)

    const campus = screen.getByLabelText('Campus')
    const unidade = screen.getByLabelText('Unidade')
    const bloco = screen.getByLabelText('Bloco')
    const andar = screen.getByLabelText('Filtrar por andar')

    expect(vemAntes(campus, unidade)).toBe(true)
    expect(vemAntes(unidade, bloco)).toBe(true)
    expect(vemAntes(bloco, andar)).toBe(true)
  })

  it('exibe inputs de localização quando initial tem campusId', () => {
    render(<PesquisaBarAmbientes initial={{ ...FILTROS_VAZIOS, campusId: 1 }} onChange={mockOnChange} />)

    expect(screen.getByLabelText('Campus')).toBeInTheDocument()
    expect(screen.getByLabelText('Unidade')).toBeInTheDocument()
    expect(screen.getByLabelText('Bloco')).toBeInTheDocument()
    expect(screen.getByLabelText('Filtrar por andar')).toBeInTheDocument()
  })

  it('exibe inputs de localização quando initial tem andar', () => {
    render(<PesquisaBarAmbientes initial={{ ...FILTROS_VAZIOS, andar: 2 }} onChange={mockOnChange} />)

    expect(screen.getByLabelText('Campus')).toBeInTheDocument()
    expect(screen.getByLabelText('Unidade')).toBeInTheDocument()
    expect(screen.getByLabelText('Bloco')).toBeInTheDocument()
    expect(screen.getByLabelText('Filtrar por andar')).toBeInTheDocument()
  })

  it('desabilita unidade e bloco quando não há campus', () => {
    render(<PesquisaBarAmbientes initial={{ ...FILTROS_VAZIOS, andar: 2 }} onChange={mockOnChange} />)

    expect(screen.getByLabelText('Campus')).toBeEnabled()
    expect(screen.getByLabelText('Unidade')).toBeDisabled()
    expect(screen.getByLabelText('Bloco')).toBeDisabled()
  })

  it('habilita unidade com campus, mas mantém bloco desabilitado sem unidade', () => {
    render(<PesquisaBarAmbientes initial={{ ...FILTROS_VAZIOS, campusId: 1 }} onChange={mockOnChange} />)

    expect(screen.getByLabelText('Unidade')).toBeEnabled()
    expect(screen.getByLabelText('Bloco')).toBeDisabled()
  })

  it('trocar o campus reseta unidade e bloco', async () => {
    const user = userEvent.setup()
    render(<PesquisaBarAmbientes initial={LOCALIZACAO_COMPLETA} onChange={mockOnChange} />)

    await user.click(screen.getByLabelText('Campus'))
    await user.click(await screen.findByRole('option', { name: 'CIDADE ALTA' }))
    await user.click(screen.getByText('Aplicar'))

    expect(mockOnChange).toHaveBeenCalledWith({
      ...FILTROS_VAZIOS,
      campusId: 2,
    })
  })

  it('trocar a unidade reseta o bloco', async () => {
    const user = userEvent.setup()
    render(<PesquisaBarAmbientes initial={LOCALIZACAO_COMPLETA} onChange={mockOnChange} />)

    await user.click(screen.getByLabelText('Unidade'))
    await user.click(await screen.findByRole('option', { name: 'UEPE' }))
    await user.click(screen.getByText('Aplicar'))

    expect(mockOnChange).toHaveBeenCalledWith({
      ...FILTROS_VAZIOS,
      campusId: 1,
      unidadeId: 11,
    })
  })

  it('Aplicar envia os IDs selecionados', () => {
    render(
      <PesquisaBarAmbientes
        initial={{ ...LOCALIZACAO_COMPLETA, andar: 2 }}
        onChange={mockOnChange}
      />,
    )

    fireEvent.click(screen.getByText('Aplicar'))

    expect(mockOnChange).toHaveBeenCalledWith({
      ...LOCALIZACAO_COMPLETA,
      andar: 2,
    })
  })

  it('não oferece "+ Cadastrar novo" nos selects do filtro', async () => {
    const user = userEvent.setup()
    render(<PesquisaBarAmbientes initial={LOCALIZACAO_COMPLETA} onChange={mockOnChange} />)

    await user.click(screen.getByLabelText('Campus'))
    expect(await screen.findByRole('option', { name: 'LIMOEIRO DO NORTE' })).toBeInTheDocument()
    expect(screen.queryByRole('option', { name: '+ Cadastrar novo' })).not.toBeInTheDocument()
    await user.keyboard('{Escape}')

    await user.click(screen.getByLabelText('Unidade'))
    expect(await screen.findByRole('option', { name: 'SEDE' })).toBeInTheDocument()
    expect(screen.queryByRole('option', { name: '+ Cadastrar novo' })).not.toBeInTheDocument()
    await user.keyboard('{Escape}')

    await user.click(screen.getByLabelText('Bloco'))
    expect(await screen.findByRole('option', { name: 'BLOCO 1' })).toBeInTheDocument()
    expect(screen.queryByRole('option', { name: '+ Cadastrar novo' })).not.toBeInTheDocument()
  })

  it('valida maxLength=50 para nome', () => {
    const initial = { ...FILTROS_VAZIOS, nome: 'Sala 101' }
    render(<PesquisaBarAmbientes initial={initial} onChange={mockOnChange} />)

    const nomeInput = screen.getByLabelText('Filtrar por nome')
    expect(nomeInput).toHaveAttribute('maxLength', '50')
  })

  it('valida min=0 para andar', () => {
    render(<PesquisaBarAmbientes initial={{ ...FILTROS_VAZIOS, campusId: 1 }} onChange={mockOnChange} />)

    const andarInput = screen.getByLabelText('Filtrar por andar')
    expect(andarInput).toHaveAttribute('min', '0')
  })

  it('sincroniza com initial externo (back/forward)', () => {
    const { rerender } = render(<PesquisaBarAmbientes initial={FILTROS_VAZIOS} onChange={mockOnChange} />)

    // Simular mudança externa (back/forward do navegador)
    const newInitial = { ...FILTROS_VAZIOS, nome: 'Sala 202' }
    rerender(<PesquisaBarAmbientes initial={newInitial} onChange={mockOnChange} />)

    // O input de nome deve estar visível
    expect(screen.getByLabelText('Filtrar por nome')).toBeInTheDocument()
  })

  it('chama onChange ao clicar em Aplicar com nome preenchido', () => {
    const initial = { ...FILTROS_VAZIOS, nome: 'Sala 101' }
    render(<PesquisaBarAmbientes initial={initial} onChange={mockOnChange} />)

    // Alterar nome
    const nomeInput = screen.getByLabelText('Filtrar por nome')
    fireEvent.change(nomeInput, { target: { value: 'Sala 202' } })

    // Clicar em Aplicar
    fireEvent.click(screen.getByText('Aplicar'))

    expect(mockOnChange).toHaveBeenCalledWith({
      ...FILTROS_VAZIOS,
      nome: 'Sala 202',
    })
  })

  it('não detecta tipo nome se nome estiver vazio', () => {
    // Renderizar com nome vazio (tipo nome NÃO deve ser detectado)
    render(<PesquisaBarAmbientes initial={{ ...FILTROS_VAZIOS, nome: '' }} onChange={mockOnChange} />)

    // O botão Aplicar deve estar desabilitado (tipo NENHUM)
    const aplicarButton = screen.getByText('Aplicar')
    expect(aplicarButton).toBeDisabled()

    // O input de nome não deve estar visível
    expect(screen.queryByLabelText('Filtrar por nome')).not.toBeInTheDocument()
  })

  it('envia o nome do enum como valor ao filtrar por tipo', async () => {
    const user = userEvent.setup()
    render(<PesquisaBarAmbientes initial={FILTROS_VAZIOS} onChange={mockOnChange} />)

    // Selecionar tipo de filtro "Tipo"
    await user.click(screen.getByLabelText('Tipo de filtro'))
    await user.click(await screen.findByRole('option', { name: 'Tipo' }))

    // Selecionar "Sala de Aula" no select de tipo
    await user.click(screen.getByLabelText('Filtrar por tipo'))
    await user.click(await screen.findByRole('option', { name: 'Sala de Aula' }))

    // Aplicar
    await user.click(screen.getByText('Aplicar'))

    expect(mockOnChange).toHaveBeenCalledWith({
      ...FILTROS_VAZIOS,
      tipo: 'SALA_AULA',
    })
  })

  it('exibe o rótulo do tipo no select quando initial tem nome do enum', () => {
    render(<PesquisaBarAmbientes initial={{ ...FILTROS_VAZIOS, tipo: 'SALA_AULA' }} onChange={mockOnChange} />)

    expect(screen.getByLabelText('Filtrar por tipo')).toBeInTheDocument()
    // O select deve exibir o rótulo "Sala de Aula" para o valor SALA_AULA
    expect(screen.getByText('Sala de Aula')).toBeInTheDocument()
  })
})
