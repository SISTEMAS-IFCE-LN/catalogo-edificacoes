import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import type { ComponentProps } from 'react'
import { AcoesAmbiente } from './AcoesAmbiente'
import type { AcaoAmbiente } from './AcoesAmbiente'

vi.mock('@/hooks/usePermission', () => ({
    usePermission: vi.fn(),
}))

import { usePermission } from '@/hooks/usePermission'

// Ações de exemplo na ordem usada pela página de detalhe (nao-publicados).
const acoesExemplo: AcaoAmbiente[] = [
    { value: 'Editar Dados Básicos', actionKey: 'ambiente:editar', onRun: vi.fn() },
    { value: 'Duplicar', actionKey: 'ambiente:duplicar', onRun: vi.fn() },
]

// Mock da permissão: por padrão todas as ações são permitidas;
// `negadas` lista os actionKeys que canDo deve rejeitar.
function mockPermissoes(negadas: string[] = []) {
    vi.mocked(usePermission).mockReturnValue({
        canDo: vi.fn((action: string) => !negadas.includes(action)),
        canAccess: vi.fn(() => true),
        hasRole: vi.fn(() => true),
    })
}

function renderComponente(props: Partial<ComponentProps<typeof AcoesAmbiente>> = {}) {
    return render(<AcoesAmbiente acoes={acoesExemplo} {...props} />)
}

describe('AcoesAmbiente', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        mockPermissoes()
    })

    it('renderiza sempre, sem depender de seleção (1 ambiente)', () => {
        renderComponente()
        expect(screen.getByRole('region', { name: 'Ações do ambiente' })).toBeInTheDocument()
        expect(screen.getByLabelText('Selecionar ação')).toBeInTheDocument()
        expect(screen.getByText('Ações do ambiente')).toBeInTheDocument()
    })

    it('botão Executar desabilitado sem opção selecionada', () => {
        renderComponente()
        expect(screen.getByRole('button', { name: 'Executar' })).toBeDisabled()
    })

    it('exibe as ações como opções ao abrir o Select', async () => {
        const user = userEvent.setup()
        renderComponente()
        await user.click(screen.getByLabelText('Selecionar ação'))
        expect(await screen.findByRole('option', { name: 'Editar Dados Básicos' })).toBeInTheDocument()
        expect(screen.getByRole('option', { name: 'Duplicar' })).toBeInTheDocument()
    })

    it('oculta opções sem permissão (canDo false para o actionKey)', async () => {
        mockPermissoes(['ambiente:duplicar'])
        const user = userEvent.setup()
        renderComponente()
        await user.click(screen.getByLabelText('Selecionar ação'))
        expect(await screen.findByRole('option', { name: 'Editar Dados Básicos' })).toBeInTheDocument()
        expect(screen.queryByRole('option', { name: 'Duplicar' })).not.toBeInTheDocument()
    })

    it('Executar chama onRun da ação selecionada e reseta para o placeholder', async () => {
        const user = userEvent.setup()
        renderComponente()
        await user.click(screen.getByLabelText('Selecionar ação'))
        await user.click(await screen.findByRole('option', { name: 'Duplicar' }))
        await user.click(screen.getByRole('button', { name: 'Executar' }))
        expect(acoesExemplo[1].onRun).toHaveBeenCalledOnce()
        expect(acoesExemplo[0].onRun).not.toHaveBeenCalled()
        // Reseta: trigger volta ao placeholder e Executar desabilita novamente.
        expect(screen.getByLabelText('Selecionar ação')).toHaveTextContent('Selecionar ação…')
        expect(screen.getByRole('button', { name: 'Executar' })).toBeDisabled()
    })

    it('Limpar reseta o Select para o placeholder', async () => {
        const user = userEvent.setup()
        renderComponente()
        await user.click(screen.getByLabelText('Selecionar ação'))
        await user.click(await screen.findByRole('option', { name: 'Duplicar' }))
        expect(screen.getByRole('button', { name: 'Executar' })).toBeEnabled()
        await user.click(screen.getByRole('button', { name: 'Limpar' }))
        expect(screen.getByLabelText('Selecionar ação')).toHaveTextContent('Selecionar ação…')
        expect(screen.getByRole('button', { name: 'Executar' })).toBeDisabled()
    })

    it('renderiza as ações críticas no slot direito', () => {
        renderComponente({
            criticalActions: <button type="button">Deletar</button>,
        })
        expect(screen.getByRole('button', { name: 'Deletar' })).toBeInTheDocument()
    })

    it('possui região acessível "Ações do ambiente"', () => {
        renderComponente()
        expect(screen.getByRole('region', { name: 'Ações do ambiente' })).toBeInTheDocument()
    })
})
