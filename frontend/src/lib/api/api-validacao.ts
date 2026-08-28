import {
    type AmbienteDetalhe,
    type AmbientesBasicosPaginados,
    type AmbientesQuery,
} from '@/types/ambientes/ambiente'
import {ROUTES} from '@/constants/routes'
import {fetchAmbientes, fetchDetalheAmbiente} from '@/lib/api/api-ambientes'
import {api} from '@/lib/api/api'

export function fetchValidacao(
    query: AmbientesQuery,
    signal?: AbortSignal
): Promise<AmbientesBasicosPaginados> {
    return fetchAmbientes(query, ROUTES.VALIDACAO, signal)
}

export function fetchDetalheValidacao(
    id: number,
    signal?: AbortSignal
): Promise<AmbienteDetalhe> {
    return fetchDetalheAmbiente(id, ROUTES.VALIDACAO, signal)
}

export async function publicarAmbiente(id: number): Promise<void> {
    await api.patch(`/api${ROUTES.VALIDACAO}/${id}/publicar`)
}

export async function privarAmbiente(id: number): Promise<void> {
    await api.patch(`/api${ROUTES.VALIDACAO}/${id}/privar`)
}
