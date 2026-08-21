import { api } from '@/lib/api/api'
import {
  AmbientesBasicosPaginadosSchema,
  EsquadriasResponseSchema,
  AmbienteDetalheSchema,
  type AmbientesBasicosPaginados,
  type AmbientesQuery,
  type EsquadriasResponse,
  type EsquadriasQuery,
  type AmbienteDetalhe,
} from '@/types/ambientes/ambiente'
import { ROUTES } from '@/constants/routes'
import { TipoFiltro } from '@/types/ambientes/enums'

export async function fetchPublicados(
  query: AmbientesQuery,
  signal?: AbortSignal
): Promise<AmbientesBasicosPaginados> {
  let url: string
  let params: Record<string, string | number | null | undefined>

  switch (query.tipoFiltro) {
    case TipoFiltro.NOME:
      url = `/api${ROUTES.PUBLICADOS}/nome`
      params = { page: query.page ?? 0, size: query.size ?? 20, nome: query.nome }
      break
    case TipoFiltro.TIPO:
      url = `/api${ROUTES.PUBLICADOS}/tipo`
      params = { page: query.page ?? 0, size: query.size ?? 20, tipo: query.tipo }
      break
    case TipoFiltro.LOCALIZACAO:
      url = `/api${ROUTES.PUBLICADOS}/localizacao`
      params = {
        page: query.page ?? 0,
        size: query.size ?? 20,
        bloco: query.bloco,
        unidade: query.unidade,
        andar: query.andar,
      }
      break
    default:
      url = `/api${ROUTES.PUBLICADOS}`
      params = { page: query.page ?? 0, size: query.size ?? 20 }
      break
  }

  const { data } = await api.get(url, {
    params,
    signal,
  })
  return AmbientesBasicosPaginadosSchema.parse(data)
}

export async function fetchDetalhePublicados(
  id: number,
  signal?: AbortSignal
): Promise<AmbienteDetalhe> {
  const { data } = await api.get(`/api${ROUTES.PUBLICADOS}/${id}`, { signal })
  return AmbienteDetalheSchema.parse(data)
}

export async function fetchEsquadriasPublicados(
  query: EsquadriasQuery,
  signal?: AbortSignal
): Promise<EsquadriasResponse> {
  const { data } = await api.get<EsquadriasResponse>(`/api${ROUTES.PUBLICADOS}/esquadrias`, {
    params: {
      ids: query.ids.join(','),
      page: query.page ?? 0,
      size: query.size ?? 100,
    },
    signal,
  })
  return EsquadriasResponseSchema.parse(data)
}