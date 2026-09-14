import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { TabelaUsuarios } from './TabelaUsuarios'
import { Role, type User } from '@/types/usuarios/user'

const usuarioAtivo: User = {
    id: 1,
    email: 'ativo@ifce.edu.br',
    nome: 'Usuário Ativo',
    ativo: true,
    criadoEm: '2025-01-01T00:00:00.000Z',
    perfis: [Role.COLABORADOR, Role.ADMINISTRADOR],
}

const usuarioInativo: User = {
    id: 2,
    email: 'inativo@ifce.edu.br',
    nome: 'Usuário Inativo',
    ativo: false,
    criadoEm: '2025-01-02T00:00:00.000Z',
    perfis: [Role.COLABORADOR],
}

describe('TabelaUsuarios', () => {
    it('renderiza a tabela desktop e os cards mobile (mesmo conteúdo em ambos viewports)', () => {
        render(
            <TabelaUsuarios
                itens={[usuarioAtivo, usuarioInativo]}
                onEditarPerfis={vi.fn()}
                onAlterarStatus={vi.fn()}
            />,
        )

        // Tabela (≥md) e cards (<md) coexistem no DOM — hidden é só CSS.
        expect(screen.getByRole('table')).toBeInTheDocument()
        expect(screen.getAllByText('Usuário Ativo')).toHaveLength(2)
        expect(screen.getAllByText('Usuário Inativo')).toHaveLength(2)
    })

    it('renderiza badges de status Ativo/Inativo', () => {
        render(
            <TabelaUsuarios
                itens={[usuarioAtivo, usuarioInativo]}
                onEditarPerfis={vi.fn()}
                onAlterarStatus={vi.fn()}
            />,
        )

        // "Ativo" = header da tabela + badge do card + badge da linha = 3.
        expect(screen.getAllByText('Ativo')).toHaveLength(3)
        // "Inativo" = badge do card + badge da linha = 2.
        expect(screen.getAllByText('Inativo')).toHaveLength(2)
    })

    it('renderiza chips de perfis por usuário', () => {
        render(
            <TabelaUsuarios
                itens={[usuarioAtivo]}
                onEditarPerfis={vi.fn()}
                onAlterarStatus={vi.fn()}
            />,
        )

        // "Administrador" e "Colaborador" aparecem no card e na linha da tabela.
        expect(screen.getAllByText('Administrador')).toHaveLength(2)
        expect(screen.getAllByText('Colaborador')).toHaveLength(2)
    })

    it('expõe nome acessível no kebab de cada usuário (card + tabela)', () => {
        render(
            <TabelaUsuarios
                itens={[usuarioAtivo, usuarioInativo]}
                onEditarPerfis={vi.fn()}
                onAlterarStatus={vi.fn()}
            />,
        )

        expect(screen.getAllByLabelText('Ações do usuário Usuário Ativo')).toHaveLength(2)
        expect(screen.getAllByLabelText('Ações do usuário Usuário Inativo')).toHaveLength(2)
    })
})
