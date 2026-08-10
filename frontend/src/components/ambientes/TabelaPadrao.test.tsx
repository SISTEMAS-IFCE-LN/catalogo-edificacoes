import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { MemoryRouter } from 'react-router'
import { TabelaPadrao } from './TabelaPadrao'
import type { AmbienteBasico } from '@/types/ambientes/ambiente'
import { Bloco, TipoAmbiente, Unidade } from '@/types/ambientes/enums'

const mockItens: AmbienteBasico[] = [
    {
        id: 1,
        nome: 'Sala 101',
        tipo: TipoAmbiente.SALA_AULA,
        localizacao: { id: 1, bloco: Bloco.BLOCO_1, unidade: Unidade.SEDE, andar: 1 },
        capacidade: 30,
        area: 50,
    },
    {
        id: 2,
        nome: 'Sala 102',
        tipo: TipoAmbiente.LABORATORIO,
        localizacao: { id: 2, bloco: Bloco.BLOCO_2, unidade: Unidade.SEDE, andar: 0 },
        capacidade: 20,
        area: 40,
    },
]

function renderWithRouter(props: React.ComponentProps<typeof TabelaPadrao>) {
    return render(
        <MemoryRouter>
            <TabelaPadrao {...props} />
        </MemoryRouter>,
    )
}

describe('TabelaPadrao — modo público (sem seleção)', () => {
    it('não renderiza coluna de checkbox quando onToggleSelect ausente', () => {
        renderWithRouter({ itens: mockItens })
        expect(screen.queryByLabelText('Selecionar todos da página')).not.toBeInTheDocument()
        expect(screen.queryByLabelText('Selecionar Sala 101')).not.toBeInTheDocument()
    })

    it('renderiza colunas padrão e link para detalhe', () => {
        renderWithRouter({ itens: mockItens })
        expect(screen.getByText('Nome')).toBeInTheDocument()
        expect(screen.getByText('Bloco')).toBeInTheDocument()
        expect(screen.getByRole('link', { name: 'Sala 101' })).toHaveAttribute(
            'href',
            '/ambientes/publicados/1',
        )
    })

    it('renderiza andar térreo', () => {
        renderWithRouter({ itens: mockItens })
        expect(screen.getByText('Térreo')).toBeInTheDocument()
    })
})

describe('TabelaPadrao — modo seleção (autenticado)', () => {
    it('renderiza checkbox no header e em cada linha quando onToggleSelect presente', () => {
        renderWithRouter({
            itens: mockItens,
            selectedIds: [],
            onToggleSelect: vi.fn(),
            onToggleSelectAll: vi.fn(),
            allSelected: false,
            someSelected: false,
        })
        expect(screen.getByLabelText('Selecionar todos da página')).toBeInTheDocument()
        expect(screen.getByLabelText('Selecionar Sala 101')).toBeInTheDocument()
        expect(screen.getByLabelText('Selecionar Sala 102')).toBeInTheDocument()
    })

    it('chama onToggleSelect ao clicar no checkbox de uma linha', () => {
        const onToggleSelect = vi.fn()
        renderWithRouter({
            itens: mockItens,
            selectedIds: [],
            onToggleSelect,
            onToggleSelectAll: vi.fn(),
            allSelected: false,
            someSelected: false,
        })
        fireEvent.click(screen.getByLabelText('Selecionar Sala 101'))
        expect(onToggleSelect).toHaveBeenCalledWith(1)
    })

    it('chama onToggleSelectAll ao clicar no checkbox do header', () => {
        const onToggleSelectAll = vi.fn()
        renderWithRouter({
            itens: mockItens,
            selectedIds: [],
            onToggleSelect: vi.fn(),
            onToggleSelectAll,
            allSelected: false,
            someSelected: false,
        })
        fireEvent.click(screen.getByLabelText('Selecionar todos da página'))
        expect(onToggleSelectAll).toHaveBeenCalled()
    })

    it('marca linha como selecionada quando id está em selectedIds', () => {
        renderWithRouter({
            itens: mockItens,
            selectedIds: [1],
            onToggleSelect: vi.fn(),
            onToggleSelectAll: vi.fn(),
            allSelected: false,
            someSelected: true,
        })
        const linhas = screen.getAllByRole('row')
        // linha 0 = header, linha 1 = Sala 101 (selecionada), linha 2 = Sala 102
        expect(linhas[1]).toHaveAttribute('data-selected', 'true')
        expect(linhas[2]).not.toHaveAttribute('data-selected')
    })

    it('mantém link para detalhe mesmo no modo seleção', () => {
        renderWithRouter({
            itens: mockItens,
            selectedIds: [],
            onToggleSelect: vi.fn(),
            onToggleSelectAll: vi.fn(),
            allSelected: false,
            someSelected: false,
        })
        expect(screen.getByRole('link', { name: 'Sala 101' })).toHaveAttribute(
            'href',
            '/ambientes/publicados/1',
        )
    })
})