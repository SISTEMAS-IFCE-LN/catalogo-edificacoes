import { render, screen, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import { PesquisaBarAmbientes } from './PesquisaBarAmbientes'
import { FILTROS_VAZIOS } from '@/types/ambientes/filtros'

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

  it('exibe inputs de localização quando initial tem bloco', () => {
    const initial = { ...FILTROS_VAZIOS, bloco: 'Bloco 1' }
    render(<PesquisaBarAmbientes initial={initial} onChange={mockOnChange} />)
    
    expect(screen.getByLabelText('Filtrar por bloco')).toBeInTheDocument()
    expect(screen.getByLabelText('Filtrar por unidade')).toBeInTheDocument()
    expect(screen.getByLabelText('Filtrar por andar')).toBeInTheDocument()
  })

  it('exibe inputs de localização quando initial tem andar', () => {
    const initial = { ...FILTROS_VAZIOS, andar: 2 }
    render(<PesquisaBarAmbientes initial={initial} onChange={mockOnChange} />)
    
    expect(screen.getByLabelText('Filtrar por bloco')).toBeInTheDocument()
    expect(screen.getByLabelText('Filtrar por unidade')).toBeInTheDocument()
    expect(screen.getByLabelText('Filtrar por andar')).toBeInTheDocument()
  })

  it('valida maxLength=50 para nome', () => {
    const initial = { ...FILTROS_VAZIOS, nome: 'Sala 101' }
    render(<PesquisaBarAmbientes initial={initial} onChange={mockOnChange} />)
    
    const nomeInput = screen.getByLabelText('Filtrar por nome')
    expect(nomeInput).toHaveAttribute('maxLength', '50')
  })

  it('valida min=0 para andar', () => {
    const initial = { ...FILTROS_VAZIOS, bloco: 'Bloco 1' }
    render(<PesquisaBarAmbientes initial={initial} onChange={mockOnChange} />)
    
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
