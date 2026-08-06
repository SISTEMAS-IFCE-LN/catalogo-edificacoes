import { api } from '@/lib/api/api'
import {
  AmbientesBasicosPaginadosSchema,
  EsquadriasResponseSchema,
  type AmbientesBasicosPaginados,
  type AmbientesQuery,
  type EsquadriasResponse,
  type EsquadriasQuery, AmbienteDetalhe
} from '@/types/ambientes/ambiente'
import { ROUTES } from '@/constants/routes'

export async function fetchAmbientes(
  query: AmbientesQuery,
  signal?: AbortSignal
): Promise<AmbientesBasicosPaginados> {
  const { data } = await api.get(`/api${ROUTES.PUBLICADOS}`, {
    params: {
      page: query.page ?? 0,
      size: query.size ?? 20,
      ...(query.nome && { nome: query.nome }),
      ...(query.bloco && { bloco: query.bloco }),
      ...(query.unidade && { unidade: query.unidade }),
      ...(query.andar != null && { andar: query.andar }),
      ...(query.tipo && { tipo: query.tipo }),
    },
    signal,
  })
  return AmbientesBasicosPaginadosSchema.parse(data)
}

export async function fetchDetalheAmbiente(id: number): Promise<AmbienteDetalhe> {
  const { data } = await api.get<AmbienteDetalhe>(`/api${ROUTES.PUBLICADOS}/${id}`)
  return data
}

export async function fetchEsquadrias(query: EsquadriasQuery): Promise<EsquadriasResponse> {
  const { data } = await api.get<EsquadriasResponse>(`/api${ROUTES.PUBLICADOS}/esquadrias`, {
    params: {
      ids: query.ids.join(','),
      page: query.page ?? 0,
      size: query.size ?? 100,
    },
  })
  return EsquadriasResponseSchema.parse(data)
}