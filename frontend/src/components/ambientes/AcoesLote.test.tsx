import { render, screen, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter } from 'react-router'
import { AcoesLote } from './AcoesLote'

const mockNavigate = vi.fn()

vi.mock('react-router', async (importOriginal) => {
    const actual = await importOriginal<typeof import('react-router')>()
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    }
})

vi.mock('@/hooks/use-mobile', () => ({
    useIsMobile: () => false,
}))

function renderWithRouter(props: React.ComponentProps<typeof AcoesLote>) {
    return render(
        <MemoryRouter>
            <AcoesLote {...props} />
        </MemoryRouter>,
    )
}

describe('AcoesLote', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('não renderiza nada quando não há seleção', () => {
        const { container } = renderWithRouter({ selectedIds: [], onClear: vi.fn() })
        expect(container.firstChild).toBeNull()
    })

    it('renderiza contador com 1 item selecionado (singular)', () => {
        renderWithRouter({ selectedIds: [1], onClear: vi.fn() })
        expect(screen.getByText('1 selecionado')).toBeInTheDocument()
    })

    it('renderiza contador com múltiplos itens (plural)', () => {
        renderWithRouter({ selectedIds: [1, 2, 3], onClear: vi.fn() })
        expect(screen.getByText('3 selecionados')).toBeInTheDocument()
    })

    it('renderiza seletor de ação com opção placeholder', () => {
        renderWithRouter({ selectedIds: [1], onClear: vi.fn() })
        expect(screen.getByLabelText('Selecionar ação em lote')).toBeInTheDocument()
        // O trigger mostra o valor selecionado atual (placeholder "NENHUMA")
        expect(screen.getByText('NENHUMA')).toBeInTheDocument()
    })

    it('renderiza ação "Detalhar Esquadrias" ao abrir o seletor', () => {
        renderWithRouter({ selectedIds: [1], onClear: vi.fn() })
        // Abrir o select
        fireEvent.click(screen.getByLabelText('Selecionar ação em lote'))
        // A opção "Detalhar Esquadrias" deve aparecer no popup
        expect(screen.getByRole('option', { name: 'Detalhar Esquadrias' })).toBeInTheDocument()
    })

    it('botão Executar está desabilitado quando nenhuma ação selecionada', () => {
        renderWithRouter({ selectedIds: [1], onClear: vi.fn() })
        expect(screen.getByText('Executar')).toBeDisabled()
    })

    it('chama onClear ao clicar em Limpar', () => {
        const onClear = vi.fn()
        renderWithRouter({ selectedIds: [1], onClear })
        fireEvent.click(screen.getByText('Limpar'))
        expect(onClear).toHaveBeenCalledOnce()
    })

    it('navega para esquadrias com ids ao selecionar ação e clicar em Executar', async () => {
        const user = userEvent.setup()
        renderWithRouter({ selectedIds: [1, 2, 3], onClear: vi.fn() })
        // Abrir o select e escolher "Detalhar Esquadrias"
        await user.click(screen.getByLabelText('Selecionar ação em lote'))
        await user.click(screen.getByRole('option', { name: 'Detalhar Esquadrias' }))
        // Botão Executar deve habilitar
        const executar = screen.getByText('Executar')
        expect(executar).not.toBeDisabled()
        await user.click(executar)
        expect(mockNavigate).toHaveBeenCalledWith(
            '/ambientes/publicados/esquadrias?ids=1,2,3',
        )
    })

    it('possui região acessível "Ações em lote"', () => {
        renderWithRouter({ selectedIds: [1], onClear: vi.fn() })
        expect(screen.getByRole('region', { name: 'Ações em lote' })).toBeInTheDocument()
    })
})