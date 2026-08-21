import {
    type AmbienteDetalhe,
    type AmbientesBasicosPaginados,
    type AmbientesQuery,
    type EsquadriasQuery,
    type EsquadriasResponse,
} from '@/types/ambientes/ambiente'
import {ROUTES} from '@/constants/routes'
import {fetchAmbientes, fetchDetalheAmbiente, fetchEsquadriasAmbientes} from '@/lib/api/api-ambientes'

export function fetchPublicados(
    query: AmbientesQuery,
    signal?: AbortSignal
): Promise<AmbientesBasicosPaginados> {
    return fetchAmbientes(query, ROUTES.PUBLICADOS, signal)
}

export function fetchDetalhePublicados(
    id: number,
    signal?: AbortSignal
): Promise<AmbienteDetalhe> {
    return fetchDetalheAmbiente(id, ROUTES.PUBLICADOS, signal)
}

export function fetchEsquadriasPublicados(
    query: EsquadriasQuery,
    signal?: AbortSignal
): Promise<EsquadriasResponse> {
    return fetchEsquadriasAmbientes(query, ROUTES.PUBLICADOS, signal)
}
