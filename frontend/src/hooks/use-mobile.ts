import * as React from 'react'

const MOBILE_BREAKPOINT = 768

/**
 * Hook que retorna `true` quando a viewport é mobile (< 768px, breakpoint `md`).
 * Padrão da CLI do shadcn. SSR-safe: assume desktop até montar no cliente.
 */
export function useIsMobile(): boolean {
    const [isMobile, setIsMobile] = React.useState<boolean>(false)

    React.useEffect(() => {
        const mql = window.matchMedia(`(max-width: ${MOBILE_BREAKPOINT - 1}px)`)
        const onChange = () => setIsMobile(mql.matches)
        onChange()
        mql.addEventListener('change', onChange)
        return () => mql.removeEventListener('change', onChange)
    }, [])

    return isMobile
}