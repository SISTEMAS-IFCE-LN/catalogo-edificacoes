import {
    type AmbienteDetalhe,
    type AmbientesBasicosPaginados,
} from '@/types/ambientes/response'
import type {AmbientesQuery, EsquadriasQuery} from '@/types/ambientes/query'
import type {EsquadriasResponse} from '@/types/ambientes/esquadrias'
import {API_ROUTES} from '@/constants/routes'
import {fetchAmbientes, fetchDetalheAmbiente, fetchEsquadriasAmbientes} from '@/lib/api/api-ambientes'

export function fetchPublicados(
    query: AmbientesQuery,
    signal?: AbortSignal
): Promise<AmbientesBasicosPaginados> {
    return fetchAmbientes(query, API_ROUTES.PUBLICADOS, signal)
}

export function fetchDetalhePublicados(
    id: number,
    signal?: AbortSignal
): Promise<AmbienteDetalhe> {
    return fetchDetalheAmbiente(id, API_ROUTES.PUBLICADOS, signal)
}

export function fetchEsquadriasPublicados(
    query: EsquadriasQuery,
    signal?: AbortSignal
): Promise<EsquadriasResponse> {
    return fetchEsquadriasAmbientes(query, API_ROUTES.PUBLICADOS, signal)
}
