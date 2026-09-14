import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import { PaginacaoFooter } from './PaginacaoFooter'
import type { ComponentProps } from 'react'

type PaginacaoFooterProps = ComponentProps<typeof PaginacaoFooter>

function renderFooter(overrides: Partial<PaginacaoFooterProps> = {}) {
    const props: PaginacaoFooterProps = {
        page: 0,
        size: 20,
        areaTotal: 90,
        hasPrevious: false,
        hasNext: true,
        currentPage: 0,
        totalPages: 2,
        onPageChange: vi.fn(),
        onSizeChange: vi.fn(),
        ...overrides,
    }
    render(<PaginacaoFooter {...props} />)
    return props
}

describe('PaginacaoFooter', () => {
    it('exibe Área Total e página corrente', () => {
        renderFooter()
        expect(screen.getByText('Área Total: 90.00 m²')).toBeInTheDocument()
        expect(screen.getByText('Página 1 de 2')).toBeInTheDocument()
    })

    it('desabilita Anterior/Próximo conforme a paginação', () => {
        renderFooter()
        expect(screen.getByRole('button', { name: 'Anterior' })).toBeDisabled()
        expect(screen.getByRole('button', { name: 'Próximo' })).toBeEnabled()
    })

    it('chama onPageChange ao clicar em Próximo', async () => {
        const user = userEvent.setup()
        const props = renderFooter()
        await user.click(screen.getByRole('button', { name: 'Próximo' }))
        expect(props.onPageChange).toHaveBeenCalledWith(1)
    })

    it('chama onSizeChange ao trocar itens por página', async () => {
        const user = userEvent.setup()
        const props = renderFooter()
        await user.click(screen.getByLabelText('Itens por página'))
        await user.click(await screen.findByRole('option', { name: '50' }))
        expect(props.onSizeChange).toHaveBeenCalledWith('50', expect.anything())
    })
})
