export const PAGES_ROUTES = {
    LOGIN: '/login',
    CALLBACK: '/callback',
    HOME: '/',
    UNAUTHORIZED: '/unauthorized',
    PUBLICADOS: '/ambientes/publicados',
    VALIDACAO: '/ambientes/validacao',
    NAO_PUBLICADOS: '/ambientes/nao-publicados',
    USUARIOS: '/usuarios',
    GOOGLE_OAUTH_ENTRY: '/oauth2/authorization/google',
} as const

export const BACKEND_URL = import.meta.env.VITE_BACKEND_URL ?? ''

const API_BASE_PATH = '/api'
const API_AMBIENTES_PATH = `${API_BASE_PATH}/ambientes`

export const API_ROUTES = {
    AUTH: '/auth',
    PUBLICADOS: `${API_AMBIENTES_PATH}/publicados`,
    VALIDACAO: `${API_AMBIENTES_PATH}/validacao`,
    NAO_PUBLICADOS: `${API_AMBIENTES_PATH}/nao-publicados`,
    USUARIOS: `${API_BASE_PATH}/usuarios`
} as const
