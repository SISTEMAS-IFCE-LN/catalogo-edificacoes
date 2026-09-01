import {useEffect, useRef} from 'react'
import {useNavigate} from 'react-router'
import {ensureCsrfToken} from '@/lib/security/csrf'
import {PAGES_ROUTES} from '@/constants/routes'
import {toast} from 'sonner'
import {useAuth} from "@/hooks/useAuth"

const FAKE_AUTH = import.meta.env.VITE_FAKE_AUTH === 'true'

export function CallbackPage() {
    const navigate = useNavigate()
    const {login} = useAuth()
    const isProcessing = useRef(false)

    useEffect(() => {
        if (isProcessing.current) return
        isProcessing.current = true

        ;(async () => {
            if (FAKE_AUTH) {
                navigate(PAGES_ROUTES.HOME, {replace: true})
                return
            }

            const hash = window.location.hash.slice(1)
            const params = new URLSearchParams(hash)
            const token = params.get('token')

            // Limpa o fragmento da URL imediatamente após ler o token,
            // evitando que o JWT fique visível na barra de endereço.
            if (window.location.hash) {
                window.history.replaceState(null, '', window.location.pathname)
            }

            if (!token) {
                toast.error('Token não recebido. Tente novamente.')
                navigate(PAGES_ROUTES.LOGIN, {replace: true})
                return
            }

            try {
                await ensureCsrfToken()
                await login(token)
                navigate(PAGES_ROUTES.HOME, {replace: true})
            } catch {
                console.error('Falha no callback OAuth2')
                toast.error('Falha ao autenticar. Tente novamente.')
                navigate(PAGES_ROUTES.LOGIN, {replace: true})
            }
        })()
    }, [login, navigate])

    return (
        <div className="min-h-screen flex items-center justify-center">
            <p className="text-muted-foreground" role="status" aria-live="polite">
                Autenticando…
            </p>
        </div>
    )
}
