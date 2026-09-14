import {vi} from 'vitest'
import '@testing-library/jest-dom/vitest'

// jsdom não implementa window.matchMedia (usado por useIsMobile, hook do
// shadcn). Mock global com matches=false (desktop) — testes que precisam da
// variante mobile sobrescrevem window.matchMedia inline (ex.:
// ResponsiveModal.test.tsx).
Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: vi.fn().mockImplementation((query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn(),
    })),
})