import axios, {
    type AxiosError,
    type AxiosRequestConfig,
    type InternalAxiosRequestConfig,
} from 'axios'
import {getAccessToken, setAccessToken, clearAccessToken} from '@/lib/security/auth'
import {getCsrfToken, ensureCsrfToken} from '@/lib/security/csrf'
import {API_ROUTES, BACKEND_URL} from "@/constants/routes";

// Cria uma instância central do axios que será usada por todo frontend
export const api = axios.create({
    baseURL: BACKEND_URL,
    withCredentials: true, // envia os cookies (refreshToken e XSRF-TOKEN)
    headers: {'Content-Type': 'application/json'},
})

// Lock para evitar refresh concorrente após respostas 401 encadeadas.
// O lock é adquirido SINCRONAMENTE: a IIFE async é invocada e atribuída
// a refreshPromise na mesma instrução, ANTES de qualquer await. Assim,
// chamadas concorrentes que cheguem durante o await ensureCsrfToken()
// encontram refreshPromise não-null e compartilham a mesma promise,
// evitando POSTs duplicados (ver docs/arquitetura-frontend.md §3.4 e §19).
let refreshPromise: Promise<string> | null = null

export function refreshAccessToken(): Promise<string> {
    if (refreshPromise) return refreshPromise

    refreshPromise = (async () => {
        // Garante que o token CSRF mascarado está em memória antes do POST /auth/refresh
        await ensureCsrfToken()
        const {data} = await axios.post(
            `${BACKEND_URL}${API_ROUTES.AUTH}/refresh`,
            {},
            {
                withCredentials: true,
                headers: {'X-XSRF-TOKEN': getCsrfToken()},
            },
        )
        const newToken: string = data.accessToken
        setAccessToken(newToken)
        return newToken
    })().finally(() => {
        refreshPromise = null
    })
    return refreshPromise
}

// Interceptador de requisições: injeta Authorization e X-XSRF-TOKEN
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    const token = getAccessToken()
    if (token) {
        config.headers.set('Authorization', `Bearer ${token}`)
    }

    const isAuthMutation =
        config.url?.startsWith(`${API_ROUTES.AUTH}/`) &&
        ['post', 'put', 'patch', 'delete'].includes(config.method ?? '')

    if (isAuthMutation) {
        const xsrf = getCsrfToken()
        if (xsrf) {
            config.headers.set('X-XSRF-TOKEN', xsrf)
        }
    }

    return config
})

// RetryConfig herda de AxiosRequestConfig e adiciona a propriedade _retry para controlar se a requisição já foi tentada novamente.
type RetryConfig = AxiosRequestConfig & { _retry?: boolean }

// Interceptador de resposta: em 401, tenta refresh e refaz a requisição original
api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const original = error.config as RetryConfig | undefined
        const isAuthEndpoint = original?.url?.startsWith(`${API_ROUTES.AUTH}/`)
        if (
            error.response?.status === 401 &&
            !isAuthEndpoint &&
            original &&
            !original._retry
        ) {
            original._retry = true
            try {
                const newToken = await refreshAccessToken()
                original.headers = {
                    ...original.headers,
                    Authorization: `Bearer ${newToken}`
                }
                return api(original)
            } catch {
                clearAccessToken()
                // O AuthProvider escuta esse evento para logout e redirecionamento
                window.dispatchEvent(new CustomEvent('auth:logout'))
                return Promise.reject(error)
            }
        }
        return Promise.reject(error)
    },
)