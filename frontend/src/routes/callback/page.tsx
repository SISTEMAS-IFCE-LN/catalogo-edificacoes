import {useEffect} from 'react'
import {useNavigate} from 'react-router'
import {ensureCsrfToken} from '@/lib/csrf'
import {ROUTES} from '@/constants/routes'
import {toast} from 'sonner'
import {useAuth} from "@/hooks/useAuth"

const FAKE_AUTH = import.meta.env.VITE_FAKE_AUTH === 'true'

export function CallbackPage() {
    const navigate = useNavigate()
    const {login} = useAuth()

    useEffect(() => {
        ;(async () => {
            if (FAKE_AUTH) {
                navigate(ROUTES.HOME, {replace: true})
                return
            }

            const hash = window.location.hash.slice(1)
            const params = new URLSearchParams(hash)
            const token = params.get('token')

            if (!token) {
                toast.error('Token não recebido. Tente novamente.')
                navigate(ROUTES.LOGIN, {replace: true})
                return
            }

            try {
                await ensureCsrfToken()
                await login(token)
                navigate(ROUTES.HOME, {replace: true})
            } catch (e) {
                console.error(e)
                toast.error('Falha ao autenticar. Tente novamente.')
                navigate(ROUTES.LOGIN, {replace: true})
            }
        })()
    }, [login, navigate])

    return (
        <div className="min-h-screen flex items-center justify-center">
            <p className="text-muted-foreground">Autenticando…</p>
        </div>
    )
}