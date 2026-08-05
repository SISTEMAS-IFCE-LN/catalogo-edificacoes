import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { PesquisaBar } from './PesquisaBar'
import { FILTROS_VAZIOS } from '@/types/ambientes/filtros'

describe('PesquisaBar', () => {
  const mockOnChange = vi.fn()

  it('renderiza seletor de tipo de filtro', () => {
    render(<PesquisaBar initial={FILTROS_VAZIOS} onChange={mockOnChange} />)
    expect(screen.getByLabelText('Tipo de filtro')).toBeInTheDocument()
  })

  it('botão Aplicar está desabilitado quando tipo=NENHUM', () => {
    render(<PesquisaBar initial={FILTROS_VAZIOS} onChange={mockOnChange} />)
    
    const aplicarButton = screen.getByText('Aplicar')
    expect(aplicarButton).toBeDisabled()
  })

  it('exibe botão Limpar quando há filtros ativos', () => {
    const initial = { ...FILTROS_VAZIOS, nome: 'Sala 101' }
    render(<PesquisaBar initial={initial} onChange={mockOnChange} />)
    
    expect(screen.getByText('Limpar')).toBeInTheDocument()
  })

  it('não exibe botão Limpar quando não há filtros', () => {
    render(<PesquisaBar initial={FILTROS_VAZIOS} onChange={mockOnChange} />)
    
    expect(screen.queryByText('Limpar')).not.toBeInTheDocument()
  })

  it('chama onChange com filtros vazios ao clicar em Limpar', () => {
    const initial = { ...FILTROS_VAZIOS, nome: 'Sala 101' }
    render(<PesquisaBar initial={initial} onChange={mockOnChange} />)
    
    fireEvent.click(screen.getByText('Limpar'))
    
    expect(mockOnChange).toHaveBeenCalledWith(FILTROS_VAZIOS)
  })

  it('exibe input de nome quando initial tem nome', () => {
    const initial = { ...FILTROS_VAZIOS, nome: 'Sala 101' }
    render(<PesquisaBar initial={initial} onChange={mockOnChange} />)
    
    expect(screen.getByLabelText('Filtrar por nome')).toBeInTheDocument()
  })

  it('exibe select de tipo quando initial tem tipo', () => {
    const initial = { ...FILTROS_VAZIOS, tipo: 'Sala de Aula' }
    render(<PesquisaBar initial={initial} onChange={mockOnChange} />)
    
    expect(screen.getByLabelText('Filtrar por tipo')).toBeInTheDocument()
  })

  it('exibe inputs de localização quando initial tem bloco', () => {
    const initial = { ...FILTROS_VAZIOS, bloco: 'Bloco 1' }
    render(<PesquisaBar initial={initial} onChange={mockOnChange} />)
    
    expect(screen.getByLabelText('Filtrar por bloco')).toBeInTheDocument()
    expect(screen.getByLabelText('Filtrar por unidade')).toBeInTheDocument()
    expect(screen.getByLabelText('Filtrar por andar')).toBeInTheDocument()
  })

  it('exibe inputs de localização quando initial tem andar', () => {
    const initial = { ...FILTROS_VAZIOS, andar: 2 }
    render(<PesquisaBar initial={initial} onChange={mockOnChange} />)
    
    expect(screen.getByLabelText('Filtrar por bloco')).toBeInTheDocument()
    expect(screen.getByLabelText('Filtrar por unidade')).toBeInTheDocument()
    expect(screen.getByLabelText('Filtrar por andar')).toBeInTheDocument()
  })

  it('valida maxLength=50 para nome', () => {
    const initial = { ...FILTROS_VAZIOS, nome: 'Sala 101' }
    render(<PesquisaBar initial={initial} onChange={mockOnChange} />)
    
    const nomeInput = screen.getByLabelText('Filtrar por nome')
    expect(nomeInput).toHaveAttribute('maxLength', '50')
  })

  it('valida min=0 para andar', () => {
    const initial = { ...FILTROS_VAZIOS, bloco: 'Bloco 1' }
    render(<PesquisaBar initial={initial} onChange={mockOnChange} />)
    
    const andarInput = screen.getByLabelText('Filtrar por andar')
    expect(andarInput).toHaveAttribute('min', '0')
  })

  it('sincroniza com initial externo (back/forward)', () => {
    const { rerender } = render(<PesquisaBar initial={FILTROS_VAZIOS} onChange={mockOnChange} />)
    
    // Simular mudança externa (back/forward do navegador)
    const newInitial = { ...FILTROS_VAZIOS, nome: 'Sala 202' }
    rerender(<PesquisaBar initial={newInitial} onChange={mockOnChange} />)
    
    // O input de nome deve estar visível
    expect(screen.getByLabelText('Filtrar por nome')).toBeInTheDocument()
  })

  it('chama onChange ao clicar em Aplicar com nome preenchido', () => {
    const initial = { ...FILTROS_VAZIOS, nome: 'Sala 101' }
    render(<PesquisaBar initial={initial} onChange={mockOnChange} />)
    
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
    render(<PesquisaBar initial={{ ...FILTROS_VAZIOS, nome: '' }} onChange={mockOnChange} />)
    
    // O botão Aplicar deve estar desabilitado (tipo NENHUM)
    const aplicarButton = screen.getByText('Aplicar')
    expect(aplicarButton).toBeDisabled()
    
    // O input de nome não deve estar visível
    expect(screen.queryByLabelText('Filtrar por nome')).not.toBeInTheDocument()
  })
})
