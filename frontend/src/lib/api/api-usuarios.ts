import {api} from '@/lib/api/api'
import type {Role, User} from '@/types/usuarios/user'
import {ROUTES} from '@/constants/routes'

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

export async function fetchUsuarios(page = 0, size = 20): Promise<UsuariosPaginados> {
    const {data} = await api.get<UsuariosPaginados>(`/api${ROUTES.USUARIOS}`, {params: {page, size}})
    return data
}

export async function fetchUsuarioPorNome(nome: string, page = 0, size = 20): Promise<UsuariosPaginados> {
    const {data} = await api.get<UsuariosPaginados>(`/api${ROUTES.USUARIOS}/nomes/${encodeURIComponent(nome)}`, {
        params: {
            page,
            size
        }
    })
    return data
}

export async function fetchUsuarioPorEmail(email: string): Promise<User> {
    const {data} = await api.get<User>(`/api${ROUTES.USUARIOS}/email/${encodeURIComponent(email)}`)
    return data
}

export async function atualizarPerfis(usuarioId: number, perfis: Role[]) {
    await api.patch(`/api${ROUTES.USUARIOS}/${usuarioId}/perfis`, perfis)
}

export async function desativarUsuario(usuarioId: number) {
    await api.patch(`/api${ROUTES.USUARIOS}/${usuarioId}/desativar`)
}

export async function ativarUsuario(usuarioId: number) {
    await api.patch(`/api${ROUTES.USUARIOS}/${usuarioId}/ativar`)
}