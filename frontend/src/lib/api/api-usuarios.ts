import {api} from '@/lib/api/api'
import type {Role, User} from '@/types/usuarios/user'
import {API_ROUTES} from '@/constants/routes'

export interface UsuariosPaginados {
    usuarios: User[]
    dadosPaginacao: {
        totalElements: number
        totalPages: number
        currentPage: number
        pageSize: number
        hasNext: boolean
        hasPrevious: boolean
    }
}

export async function fetchUsuarios(page = 0, size = 20, signal?: AbortSignal): Promise<UsuariosPaginados> {
    const {data} = await api.get<UsuariosPaginados>(`${API_ROUTES.USUARIOS}`, {params: {page, size}, signal})
    return data
}

export async function fetchUsuarioPorNome(nome: string, page = 0, size = 20, signal?: AbortSignal): Promise<UsuariosPaginados> {
    const {data} = await api.get<UsuariosPaginados>(`${API_ROUTES.USUARIOS}/nomes/${encodeURIComponent(nome)}`, {
        params: {
            page,
            size
        },
        signal,
    })
    return data
}

export async function fetchUsuarioPorEmail(email: string, signal?: AbortSignal): Promise<User> {
    const {data} = await api.get<User>(`${API_ROUTES.USUARIOS}/email/${encodeURIComponent(email)}`, {signal})
    return data
}

export async function atualizarPerfis(usuarioId: number, perfis: Role[]) {
    await api.patch(`${API_ROUTES.USUARIOS}/${usuarioId}/perfis`, perfis)
}

export async function desativarUsuario(usuarioId: number) {
    await api.patch(`${API_ROUTES.USUARIOS}/${usuarioId}/desativar`)
}

export async function ativarUsuario(usuarioId: number) {
    await api.patch(`${API_ROUTES.USUARIOS}/${usuarioId}/ativar`)
}