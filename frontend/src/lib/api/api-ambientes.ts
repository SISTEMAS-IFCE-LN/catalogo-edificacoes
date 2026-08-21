import {
    type AmbienteDetalhe,
    AmbienteDetalheSchema,
    AmbientesBasicosPaginados,
    AmbientesBasicosPaginadosSchema,
    AmbientesQuery,
    type EsquadriasQuery,
    type EsquadriasResponse,
    EsquadriasResponseSchema
} from '@/types/ambientes/ambiente'
import {TipoFiltro} from '@/types/ambientes/enums'
import {api} from '@/lib/api/api'

interface QueryComponents {
    url: string
    params: Record<string, string | number | null | undefined>
}

function definirQueryComponents(query: AmbientesQuery, route: string): QueryComponents {
    const queryComponents: QueryComponents = {
        url: '',
        params: {},
    }
    switch (query.tipoFiltro) {
        case TipoFiltro.NOME:
            queryComponents.url = `/api${route}/nome`
            queryComponents.params = {page: query.page ?? 0, size: query.size ?? 20, nome: query.nome}
            break
        case TipoFiltro.TIPO:
            queryComponents.url = `/api${route}/tipo`
            queryComponents.params = {page: query.page ?? 0, size: query.size ?? 20, tipo: query.tipo}
            break
        case TipoFiltro.LOCALIZACAO:
            queryComponents.url = `/api${route}/localizacao`
            queryComponents.params = {
                page: query.page ?? 0,
                size: query.size ?? 20,
                bloco: query.bloco,
                unidade: query.unidade,
                andar: query.andar,
            }
            break
        default:
            queryComponents.url = `/api${route}`
            queryComponents.params = {page: query.page ?? 0, size: query.size ?? 20}
            break
    }
    return queryComponents
}

export async function fetchAmbientes(
    query: AmbientesQuery,
    route: string,
    signal?: AbortSignal
): Promise<AmbientesBasicosPaginados> {

    const {url, params} = definirQueryComponents(query, route)

    const {data} = await api.get(url, {
        params,
        signal,
    })
    return AmbientesBasicosPaginadosSchema.parse(data)
}

export async function fetchDetalheAmbientes(
    id: number,
    route: string,
    signal?: AbortSignal
): Promise<AmbienteDetalhe> {
    const {data} = await api.get(`/api${route}/${id}`, {signal})
    return AmbienteDetalheSchema.parse(data)
}

export async function fetchEsquadriasAmbientes(
    query: EsquadriasQuery,
    route: string,
    signal?: AbortSignal
): Promise<EsquadriasResponse> {
    const {data} = await api.get<EsquadriasResponse>(`/api${route}/esquadrias`, {
        params: {
            ids: query.ids.join(','),
            page: query.page ?? 0,
            size: query.size ?? 100,
        },
        signal,
    })
    return EsquadriasResponseSchema.parse(data)
}