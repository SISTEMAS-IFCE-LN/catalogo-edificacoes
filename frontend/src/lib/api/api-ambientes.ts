import { api } from '@/lib/api/api'
import { AmbientesBasicosPaginadosSchema, type AmbientesBasicosPaginados } from '@/types/ambientes/ambiente'
import { ROUTES } from '@/constants/routes'

export interface PublicadosQuery {
  page?: number
  size?: number
  nome?: string
  bloco?: string
  unidade?: string
  andar?: number
  tipo?: string
}

export async function fetchPublicados(
  query: PublicadosQuery,
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