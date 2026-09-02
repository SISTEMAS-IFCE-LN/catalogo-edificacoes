import {
    type AmbienteDetalhe,
    AmbienteDetalheSchema,
    type AmbientesBasicosPaginados,
    AmbientesBasicosPaginadosSchema,
} from '@/types/ambientes/response'
import type {AmbientesQuery, EsquadriasQuery} from '@/types/ambientes/query'
import {type EsquadriasResponse, EsquadriasResponseSchema} from '@/types/ambientes/esquadrias'
import {TipoFiltro} from '@/types/ambientes/enums'
import {api} from '@/lib/api/api'

interface QueryComponents {
    url: string
    params: Record<string, string | number | null | undefined>
}

function definirQueryComponents(query: AmbientesQuery, apiRoute: string): QueryComponents {
    const queryComponents: QueryComponents = {
        url: '',
        params: {},
    }
    switch (query.tipoFiltro) {
        case TipoFiltro.NOME:
            queryComponents.url = `${apiRoute}/nome`
            queryComponents.params = {page: query.page ?? 0, size: query.size ?? 20, nome: query.nome}
            break
        case TipoFiltro.TIPO:
            queryComponents.url = `${apiRoute}/tipo`
            queryComponents.params = {page: query.page ?? 0, size: query.size ?? 20, tipo: query.tipo}
            break
        case TipoFiltro.LOCALIZACAO:
            queryComponents.url = `${apiRoute}/localizacao`
            queryComponents.params = {
                page: query.page ?? 0,
                size: query.size ?? 20,
                bloco: query.bloco,
                unidade: query.unidade,
                andar: query.andar,
            }
            break
        default:
            queryComponents.url = `${apiRoute}`
            queryComponents.params = {page: query.page ?? 0, size: query.size ?? 20}
            break
    }
    return queryComponents
}

export async function fetchAmbientes(
    query: AmbientesQuery,
    apiRoute: string,
    signal?: AbortSignal
): Promise<AmbientesBasicosPaginados> {

    const {url, params} = definirQueryComponents(query, apiRoute)

    const {data} = await api.get(url, {
        params,
        signal,
    })
    return AmbientesBasicosPaginadosSchema.parse(data)
}

export async function fetchDetalheAmbiente(
    id: number,
    apiRoute: string,
    signal?: AbortSignal
): Promise<AmbienteDetalhe> {
    const {data} = await api.get(`${apiRoute}/${id}`, {signal})
    return AmbienteDetalheSchema.parse(data)
}

export async function fetchEsquadriasAmbientes(
    query: EsquadriasQuery,
    apiRoute: string,
    signal?: AbortSignal
): Promise<EsquadriasResponse> {
    const {data} = await api.get<EsquadriasResponse>(`${apiRoute}/esquadrias`, {
        params: {
            ids: query.ids.join(','),
            page: query.page ?? 0,
            size: query.size ?? 100,
        },
        signal,
    })
    return EsquadriasResponseSchema.parse(data)
}
