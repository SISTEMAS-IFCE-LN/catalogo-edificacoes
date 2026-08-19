import { render, screen, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import { PesquisaBarUsuarios } from './PesquisaBarUsuarios'
import { FILTROS_USUARIOS_VAZIOS } from '@/types/usuarios/filtros'

describe('PesquisaBarUsuarios', () => {
  const mockOnChange = vi.fn()

  it('renderiza seletor de tipo de filtro', () => {
    render(<PesquisaBarUsuarios initial={FILTROS_USUARIOS_VAZIOS} onChange={mockOnChange} />)
    expect(screen.getByLabelText('Tipo de filtro')).toBeInTheDocument()
  })

  it('botão Buscar está desabilitado quando tipo=NENHUM', () => {
    render(<PesquisaBarUsuarios initial={FILTROS_USUARIOS_VAZIOS} onChange={mockOnChange} />)

    expect(screen.getByText('Buscar')).toBeDisabled()
  })

  it('exibe botão Limpar quando há filtros ativos (nome)', () => {
    render(<PesquisaBarUsuarios initial={{ ...FILTROS_USUARIOS_VAZIOS, nome: 'João' }} onChange={mockOnChange} />)

    expect(screen.getByText('Limpar')).toBeInTheDocument()
  })

  it('não exibe botão Limpar quando não há filtros', () => {
    render(<PesquisaBarUsuarios initial={FILTROS_USUARIOS_VAZIOS} onChange={mockOnChange} />)

    expect(screen.queryByText('Limpar')).not.toBeInTheDocument()
  })

  it('exibe input de nome quando initial tem nome', () => {
    render(<PesquisaBarUsuarios initial={{ ...FILTROS_USUARIOS_VAZIOS, nome: 'João' }} onChange={mockOnChange} />)

    expect(screen.getByLabelText('Buscar usuário por nome')).toBeInTheDocument()
  })

  it('exibe input de email quando initial tem email', () => {
    render(
      <PesquisaBarUsuarios
        initial={{ ...FILTROS_USUARIOS_VAZIOS, email: 'joao@ifce.edu.br' }}
        onChange={mockOnChange}
      />,
    )

    expect(screen.getByLabelText('Buscar usuário por email')).toBeInTheDocument()
  })

  it('chama onChange ao clicar em Buscar com nome preenchido', async () => {
    const user = userEvent.setup()
    render(<PesquisaBarUsuarios initial={FILTROS_USUARIOS_VAZIOS} onChange={mockOnChange} />)

    await user.click(screen.getByLabelText('Tipo de filtro'))
    await user.click(await screen.findByRole('option', { name: 'Nome' }))

    fireEvent.change(screen.getByLabelText('Buscar usuário por nome'), { target: { value: 'João' } })
    fireEvent.click(screen.getByText('Buscar'))

    expect(mockOnChange).toHaveBeenCalledWith({ nome: 'João', email: '' })
  })

  it('chama onChange ao clicar em Buscar com email preenchido', async () => {
    const user = userEvent.setup()
    render(<PesquisaBarUsuarios initial={FILTROS_USUARIOS_VAZIOS} onChange={mockOnChange} />)

    await user.click(screen.getByLabelText('Tipo de filtro'))
    await user.click(await screen.findByRole('option', { name: 'Email' }))

    fireEvent.change(screen.getByLabelText('Buscar usuário por email'), {
      target: { value: 'joao@ifce.edu.br' },
    })
    fireEvent.click(screen.getByText('Buscar'))

    expect(mockOnChange).toHaveBeenCalledWith({ nome: '', email: 'joao@ifce.edu.br' })
  })

  it('chama onChange com filtros vazios ao clicar em Limpar', () => {
    render(<PesquisaBarUsuarios initial={{ ...FILTROS_USUARIOS_VAZIOS, nome: 'João' }} onChange={mockOnChange} />)

    fireEvent.click(screen.getByText('Limpar'))

    expect(mockOnChange).toHaveBeenCalledWith(FILTROS_USUARIOS_VAZIOS)
  })
})
