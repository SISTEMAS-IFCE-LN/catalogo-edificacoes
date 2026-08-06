import {z} from 'zod'
import {
    Bloco,
    MaterialEsquadria,
    StatusAmbiente,
    TipoAmbiente,
    TipoEsquadria,
    TipoGeometria, Unidade
} from '@/types/ambientes/enums'

const LocalizacaoSchema = z.object({
    id: z.number(),
    bloco: z.enum(Bloco),
    unidade: z.enum(Unidade),
    andar: z.int(),
})

const AmbienteBasicoSchema = z.object({
    id: z.number(),
    nome: z.string(),
    tipo: z.enum(TipoAmbiente),
    localizacao: LocalizacaoSchema,
    capacidade: z.int(),
    area: z.number(),
})

export type AmbienteBasico = z.infer<typeof AmbienteBasicoSchema>

export const AmbientesBasicosPaginadosSchema = z.object({
    ambientes: z.array(AmbienteBasicoSchema),
    areaTotal: z.number(),
    dadosPaginacao: z.object({
        totalElements: z.int(),
        totalPages: z.int(),
        currentPage: z.int(),
        pageSize: z.int(),
        hasNext: z.boolean(),
        hasPrevious: z.boolean(),
    }),
})

export type AmbientesBasicosPaginados = z.infer<typeof AmbientesBasicosPaginadosSchema>

// Geometria do ambiente (GeometriaAmbienteRes)
const GeometriaAmbienteSchema = z.object({
    id: z.number(),
    tipo: z.enum(TipoGeometria),
    base: z.number(),
    altura: z.number(),
    repeticao: z.int(),
    area: z.number(),
})

// Geometria da esquadria (GeometriaEsquadriaRes)
const GeometriaEsquadriaSchema = z.object({
    id: z.number(),
    base: z.number(),
    altura: z.number(),
    repeticao: z.int(),
    area: z.number(),
})

// Esquadria (EsquadriaRes)
const EsquadriaSchema = z.object({
    id: z.number(),
    tipo: z.enum(TipoEsquadria),
    geometria: GeometriaEsquadriaSchema,
    alturaPeitoril: z.number(),
    area: z.number(),
    material: z.enum(MaterialEsquadria),
    informacaoAdicional: z.string(),
})

// Resumo por tipo/material (EsquadriaTipoMaterialRes)
const EsquadriaTipoMaterialSchema = z.object({
    tipo: z.enum(TipoEsquadria),
    material: z.enum(MaterialEsquadria),
    area: z.number(),
})

// Detalhes de esquadrias (EsquadriasDetalhesRes)
const EsquadriasDetalhesSchema = z.object({
    esquadrias: z.array(EsquadriaSchema),
    esquadriasTipoMaterial: z.array(EsquadriaTipoMaterialSchema),
})

// Ambiente detalhado (AmbienteRes)
// eslint-disable-next-line @typescript-eslint/no-unused-vars -- será usado na parte 08 para validar resposta da API
const AmbienteDetalheSchema = AmbienteBasicoSchema.extend({
    geometrias: z.array(GeometriaAmbienteSchema),
    areaAmbiente: z.number(),
    pesDireitos: z.array(z.number()),
    esquadriasDetalhes: EsquadriasDetalhesSchema,
    informacaoAdicional: z.string(),
    status: z.enum(StatusAmbiente),
})

export type AmbienteDetalhe = z.infer<typeof AmbienteDetalheSchema>

// Ambiente com nome e localização (para UC20-FE)
const AmbienteNomeLocalizacaoSchema = z.object({
    id: z.number(),
    nome: z.string(),
    localizacao: LocalizacaoSchema,
})

// Ambiente com esquadrias (para UC20-FE)
const AmbienteEsquadriasSchema = z.object({
    dadosAmbiente: AmbienteNomeLocalizacaoSchema,
    detalhesEsquadrias: EsquadriasDetalhesSchema,
})

// Resposta de esquadrias (para UC20-FE)
export const EsquadriasResponseSchema = z.object({
    ambientes: z.array(AmbienteEsquadriasSchema),
    totalTipoMaterial: z.array(EsquadriaTipoMaterialSchema),
    dadosPaginacao: z.object({
        totalElements: z.int(),
        totalPages: z.int(),
        currentPage: z.int(),
        pageSize: z.int(),
        hasNext: z.boolean(),
        hasPrevious: z.boolean(),
    }),
})

export type EsquadriasResponse = z.infer<typeof EsquadriasResponseSchema>

// Query parameters para listagem de ambientes
export interface AmbientesQuery {
    page?: number
    size?: number
    nome?: string
    bloco?: string
    unidade?: string
    andar?: number
    tipo?: string
}

// Query parameters para UC20-FE
export interface EsquadriasQuery {
    ids: number[]
    page?: number
    size?: number
}