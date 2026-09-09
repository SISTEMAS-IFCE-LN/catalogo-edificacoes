import {render, screen, within} from '@testing-library/react'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {ResponsiveModal} from './ResponsiveModal'
import {Button} from '@/components/ui/button'

// jsdom não implementa matchMedia; useIsMobile consulta `(max-width: 767px)`.
// Sobrescreve o mock global do setup por teste (matches=true → mobile).
function mockarMatchMedia(matches: boolean) {
    Object.defineProperty(window, 'matchMedia', {
        writable: true,
        value: vi.fn().mockImplementation((query: string) => ({
            matches,
            media: query,
            onchange: null,
            addListener: vi.fn(),
            removeListener: vi.fn(),
            addEventListener: vi.fn(),
            removeEventListener: vi.fn(),
            dispatchEvent: vi.fn(),
        })),
    })
}

function renderModal(matches: boolean) {
    mockarMatchMedia(matches)
    return render(
        <ResponsiveModal
            open
            onOpenChange={vi.fn()}
            title="Título do modal"
            description="Descrição do modal"
            footer={<Button type="button">Salvar</Button>}
        >
            <div>Conteúdo do modal</div>
        </ResponsiveModal>,
    )
}

describe('ResponsiveModal', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('desktop (≥768px): renderiza Dialog com footer fora do container rolante', () => {
        renderModal(false)

        expect(document.querySelector('[data-slot="dialog-content"]')).not.toBeNull()
        expect(screen.getByText('Título do modal')).toBeInTheDocument()
        expect(screen.getByText('Descrição do modal')).toBeInTheDocument()
        expect(screen.getByText('Conteúdo do modal')).toBeInTheDocument()

        // Footer dentro do dialog, mas fora do corpo rolante (prop footer)
        const footer = document.querySelector('[data-slot="dialog-footer"]')
        expect(footer).not.toBeNull()
        expect(within(footer as HTMLElement).getByRole('button', {name: 'Salvar'})).toBeInTheDocument()
    })

    it('mobile (<768px): renderiza Drawer com título, corpo e footer', async () => {
        renderModal(true)

        expect(await screen.findByText('Título do modal')).toBeInTheDocument()
        expect(document.querySelector('[data-slot="drawer-popup"]')).not.toBeNull()
        expect(screen.getByText('Conteúdo do modal')).toBeInTheDocument()
        expect(screen.getByRole('button', {name: 'Salvar'})).toBeInTheDocument()
    })

    it('regressão do bug: com children altos, os botões do footer permanecem no DOM dentro do dialog', () => {
        mockarMatchMedia(false)
        render(
            <ResponsiveModal
                open
                onOpenChange={vi.fn()}
                title="Título do modal"
                footer={
                    <>
                        <Button type="button" variant="outline">Cancelar</Button>
                        <Button type="submit">Salvar</Button>
                    </>
                }
            >
                {Array.from({length: 30}, (_, i) => (
                    <div key={i}>Item {i + 1}</div>
                ))}
            </ResponsiveModal>,
        )

        const dialog = document.querySelector('[data-slot="dialog-content"]')
        expect(dialog).not.toBeNull()
        expect(within(dialog as HTMLElement).getByRole('button', {name: 'Salvar'})).toBeInTheDocument()
        expect(within(dialog as HTMLElement).getByRole('button', {name: 'Cancelar'})).toBeInTheDocument()
        expect(screen.getByText('Item 30')).toBeInTheDocument()
    })
})