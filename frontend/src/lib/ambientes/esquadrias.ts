import type {
    AmbienteEsquadrias,
    Esquadria,
    EsquadriaTipoMaterial,
    EsquadriasResponse,
} from '@/types/ambientes/esquadrias'

export interface FiltroEsquadrias {
    tipo?: string
    material?: string
}

export const FILTRO_ESQUADRIAS_VAZIO: FiltroEsquadrias = {
    tipo: '',
    material: '',
}

/**
 * Verifica se o filtro está vazio (nenhum critério aplicado).
 */
export function filtroEsquadriasVazio(filtro: FiltroEsquadrias): boolean {
    return !filtro.tipo && !filtro.material
}

/**
 * Filtra as esquadrias de cada ambiente por tipo e/ou material (client-side).
 * Recalcula o resumo por tipo/material de cada ambiente com base nas
 * esquadrias remanescentes. O resumo global `totalTipoMaterial` é mantido
 * intacto pois reflete o agregado de todos os ambientes da página atual
 * (não há como recomputá-lo fielmente sem os dados brutos do backend).
 *
 * Retorna `null` quando o filtro é vazio (sem necessidade de cópia).
 */
export function filtrarEsquadrias(
    response: EsquadriasResponse,
    filtro: FiltroEsquadrias,
): EsquadriasResponse {
    if (filtroEsquadriasVazio(filtro)) {
        return response
    }

    const ambientesFiltrados = response.ambientes.map((amb) => {
        const esquadrias = amb.detalhesEsquadrias.esquadrias.filter((e) => {
            const matchesTipo = !filtro.tipo || e.tipo === filtro.tipo
            const matchesMaterial = !filtro.material || e.material === filtro.material
            return matchesTipo && matchesMaterial
        })

        return {
            dadosAmbiente: amb.dadosAmbiente,
            detalhesEsquadrias: {
                esquadrias,
                esquadriasTipoMaterial: resumirPorTipoMaterial(esquadrias),
            },
        }
    })

    return {
        ambientes: ambientesFiltrados,
        totalTipoMaterial: response.totalTipoMaterial,
        dadosPaginacao: response.dadosPaginacao,
    }
}

/**
 * Recalcula o resumo por tipo/material a partir de uma lista de esquadrias.
 * Soma as áreas agrupadas por (tipo, material).
 */
export function resumirPorTipoMaterial(esquadrias: Esquadria[]): EsquadriaTipoMaterial[] {
    const mapa = new Map<string, EsquadriaTipoMaterial>()
    for (const e of esquadrias) {
        const chave = `${e.tipo}-${e.material}`
        const atual = mapa.get(chave)
        if (atual) {
            mapa.set(chave, { ...atual, area: atual.area + e.area })
        } else {
            mapa.set(chave, {
                tipo: e.tipo,
                material: e.material,
                area: e.area,
            })
        }
    }
    return Array.from(mapa.values())
}

/**
 * Compara os IDs solicitados com os IDs retornados pelo backend.
 * IDs presentes na requisição mas ausentes na resposta são considerados
 * inválidos (não correspondem a um ambiente publicado ou não possuem
 * esquadrias associadas).
 */
export function obterIdsInvalidos(solicitados: number[], response: EsquadriasResponse): number[] {
    const retornados = new Set(response.ambientes.map((a) => a.dadosAmbiente.id))
    return solicitados.filter((id) => !retornados.has(id))
}

/**
 * Verifica se há ao menos uma esquadria em todos os ambientes filtrados.
 * Usado para exibir o callout "Nenhuma esquadria encontrada" pós-filtro.
 */
export function temEsquadriasVisiveis(ambientes: AmbienteEsquadrias[]): boolean {
    return ambientes.some((a) => a.detalhesEsquadrias.esquadrias.length > 0)
}