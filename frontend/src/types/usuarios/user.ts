export enum Role {
    COLABORADOR = 'ROLE_COLABORADOR',
    VALIDADOR = 'ROLE_VALIDADOR',
    GESTOR_SISTEMA = 'ROLE_GESTOR_SISTEMA',
    ADMINISTRADOR = 'ROLE_ADMINISTRADOR',
}

export interface User {
    id: number
    email: string
    nome: string
    ativo: boolean
    criadoEm: string
    perfis: Role[]
}

export interface AuthState {
    user: User | null
    isAuthenticated: boolean
    isLoading: boolean
}

export type StatusAcao = 'desativar' | 'ativar'