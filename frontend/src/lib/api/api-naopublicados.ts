import {api} from '@/lib/api/api'
import {
    type AmbienteDetalhe,
    AmbienteDetalheSchema,
    type AmbientesBasicosPaginados,
    type AmbientesQuery,
} from '@/types/ambientes/ambiente'
import {API_ROUTES} from '@/constants/routes'
import {fetchAmbientes, fetchDetalheAmbiente} from '@/lib/api/api-ambientes'
import {type AmbienteInput, type EsquadriaInput, type GeometriaInput, type LocalizacaoInput,} from '@/schemas/ambiente'

export function fetchNaoPublicados(
    query: AmbientesQuery,
    signal?: AbortSignal
): Promise<AmbientesBasicosPaginados> {
    return fetchAmbientes(query, API_ROUTES.NAO_PUBLICADOS, signal)
}

export function fetchAmbienteNaoPublicado(
    id: number,
    signal?: AbortSignal
): Promise<AmbienteDetalhe> {
    return fetchDetalheAmbiente(id, API_ROUTES.NAO_PUBLICADOS, signal)
}

export async function criarAmbiente(payload: AmbienteInput): Promise<AmbienteDetalhe> {
    const {data} = await api.post(`${API_ROUTES.NAO_PUBLICADOS}`, payload)
    return AmbienteDetalheSchema.parse(data)
}

export async function atualizarDadosBasicos(
    id: number,
    payload: { nome: string; localizacao: LocalizacaoInput; capacidade: number }
): Promise<void> {
    await api.patch(`${API_ROUTES.NAO_PUBLICADOS}/${id}/dados-basicos`, payload)
}

export async function incluirGeometrias(id: number, geometrias: GeometriaInput[]): Promise<void> {
    await api.patch(`${API_ROUTES.NAO_PUBLICADOS}/${id}/geometrias/incluir`, geometrias)
}

export async function atualizarGeometrias(id: number, geometrias: GeometriaInput[]): Promise<void> {
    await api.patch(`${API_ROUTES.NAO_PUBLICADOS}/${id}/geometrias/atualizar`, geometrias)
}

export async function incluirPesDireitos(id: number, alturas: number[]): Promise<void> {
    await api.patch(`${API_ROUTES.NAO_PUBLICADOS}/${id}/pes-direitos/incluir`, alturas)
}

export async function atualizarPesDireitos(id: number, alturas: number[]): Promise<void> {
    await api.patch(`${API_ROUTES.NAO_PUBLICADOS}/${id}/pes-direitos/atualizar`, alturas)
}

export async function incluirEsquadrias(id: number, esquadrias: EsquadriaInput[]): Promise<void> {
    await api.patch(`${API_ROUTES.NAO_PUBLICADOS}/${id}/esquadrias/incluir`, esquadrias)
}

export async function atualizarEsquadrias(id: number, esquadrias: EsquadriaInput[]): Promise<void> {
    await api.patch(`${API_ROUTES.NAO_PUBLICADOS}/${id}/esquadrias/atualizar`, esquadrias)
}

export async function atualizarInfoAdicional(id: number, info: string): Promise<void> {
    await api.patch(`${API_ROUTES.NAO_PUBLICADOS}/${id}/informacao-adicional`, info, {
        headers: {'Content-Type': 'text/plain'},
    })
}

export async function deletarAmbientes(ids: number[]): Promise<void> {
    await api.delete(`${API_ROUTES.NAO_PUBLICADOS}`, {data: ids})
}

export async function alterarTipo(id: number, payload: AmbienteInput): Promise<AmbienteDetalhe> {
    const {data} = await api.post(`${API_ROUTES.NAO_PUBLICADOS}/${id}`, payload)
    return AmbienteDetalheSchema.parse(data)
}

export async function duplicarAmbiente(id: number, payload: {
    nome: string;
    localizacao: LocalizacaoInput
}): Promise<AmbienteDetalhe> {
    const {data} = await api.post(`${API_ROUTES.NAO_PUBLICADOS}/${id}/duplicar`, payload)
    return AmbienteDetalheSchema.parse(data)
}

export async function enviarParaValidacao(ids: number[]): Promise<void> {
    await api.patch(`${API_ROUTES.NAO_PUBLICADOS}/validar`, ids)
}