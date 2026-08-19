import {
    useCallback,
    useEffect,
    useMemo,
    useState,
} from 'react'
import type {ReactNode} from 'react'
import type {AuthState, User} from '@/types/usuarios/user'
import {api, refreshAccessToken} from '@/lib/api/api'
import {
    clearAccessToken,
    getAccessToken,
    setAccessToken,
} from '@/lib/security/auth'
import {ensureCsrfToken, clearCsrfToken} from '@/lib/security/csrf'
import { AuthContext } from './AuthContext'
import type { AuthContextValue } from './AuthContext'

const FAKE_AUTH = import.meta.env.VITE_FAKE_AUTH === 'true'

const FAKE_USER: User = {
    id: 1,
    email: 'dev@ifce.edu.br',
    nome: 'Dev FakeAuth',
    ativo: true,
    criadoEm: new Date().toISOString(),
    perfis: [
        'ROLE_COLABORADOR',
        'ROLE_VALIDADOR',
        'ROLE_GESTOR_SISTEMA',
        'ROLE_ADMINISTRADOR',
    ] as User['perfis'],
}

const INITIAL_STATE: AuthState = FAKE_AUTH
    ? { user: FAKE_USER, isAuthenticated: true, isLoading: false }
    : { user: null, isAuthenticated: false, isLoading: true }

export function AuthProvider({children}: { children: ReactNode }) {
    const [state, setState] = useState<AuthState>(INITIAL_STATE)

    const loadUser = useCallback(async () => {
        const {data} = await api.get<User>('/api/usuarios/me')
        setState((s) => ({
            ...s,
            user: data,
            isAuthenticated: true,
            isLoading: false,
        }))
    }, [])

    // Boot
    useEffect(() => {
        if (FAKE_AUTH) {
            console.warn('[FakeAuth] ATIVADO — não usar em produção!')
            return
        }

        let cancelled = false
        ;(async () => {
            try {
                if (!getAccessToken()) {
                    await refreshAccessToken()
                }
                await loadUser()
            } catch {
                clearAccessToken()
                if (!cancelled) {
                    setState((s) => ({
                        ...s,
                        isLoading: false,
                        isAuthenticated: false,
                    }))
                }
            }
        })()

        return () => {
            cancelled = true
        }
    }, [loadUser])

    // Escuta o evento disparado pelo interceptor Axios em caso de refresh falho
    useEffect(() => {
        const onLogout = () => {
            clearAccessToken()
            clearCsrfToken()
            setState({
                user: null,
                isAuthenticated: false,
                isLoading: false,
            })
        }
        window.addEventListener('auth:logout', onLogout)
        return () => window.removeEventListener('auth:logout', onLogout)
    }, [])

    const login = useCallback(
        async (token: string) => {
            setAccessToken(token)
            await loadUser()
        },
        [loadUser],
    )

    const logout = useCallback(async () => {
        if (!FAKE_AUTH) {
            try {
                await ensureCsrfToken()
                await api.post('/auth/logout')
            } catch (e) {
                console.warn(
                    'Logout backend falhou — limpando estado local',
                    e instanceof Error ? e.message : String(e),
                )
            }
        }
        clearAccessToken()
        clearCsrfToken()
        setState({
            user: null,
            isAuthenticated: false,
            isLoading: false,
        })
    }, [])

    const refreshUser = useCallback(async () => {
        if (getAccessToken()) await loadUser()
    }, [loadUser])

    const value = useMemo<AuthContextValue>(
        () => ({...state, login, logout, refreshUser}),
        [state, login, logout, refreshUser]
    )

    return (
        <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
    )
}