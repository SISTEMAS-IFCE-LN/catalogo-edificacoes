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

// O DTO de detalhe do backend (AmbienteRes) não retorna `area`.
// A área calculada do ambiente é representada por `areaAmbiente` no detalhe.
const AmbienteDetalheBaseSchema = AmbienteBasicoSchema.omit({area: true})

// Os DTOs de detalhe retornam os nomes dos enums Kotlin (por exemplo,
// `RETANGULAR`), enquanto a lista retorna os rótulos (`Retangular`).
// Normalizamos ambos para os valores exibidos pelo frontend.
const TipoGeometriaResponseSchema = z.union([
    z.enum(TipoGeometria),
    z.enum(['RETANGULAR', 'TRIANGULAR']),
]).transform((value) => (
    value in TipoGeometria
        ? TipoGeometria[value as keyof typeof TipoGeometria]
        : value
))

const TipoEsquadriaResponseSchema = z.union([
    z.enum(TipoEsquadria),
    z.enum(['PORTA', 'JANELA', 'COBOGO', 'VAO_ABERTO', 'ESQUADRIA_OUTRO_AMBIENTE']),
]).transform((value) => (
    value in TipoEsquadria
        ? TipoEsquadria[value as keyof typeof TipoEsquadria]
        : value
))

const MaterialEsquadriaResponseSchema = z.union([
    z.enum(MaterialEsquadria),
    z.enum([
        'ALUMINIO',
        'ALUMINIO_VIDRO',
        'ALUMINIO_PVC',
        'FERRO',
        'FERRO_VIDRO',
        'VIDRO',
        'PVC',
        'MADEIRA_MACICA',
        'MADEIRA_VIDRO',
        'MADEIRA_VENEZIANA',
        'MADEIRA_FICHA',
        'MADEIRA_PARANA',
        'PRE_MOLDADO',
        'NAO_SE_APLICA',
        'OUTRO',
    ]),
]).transform((value) => (
    value in MaterialEsquadria
        ? MaterialEsquadria[value as keyof typeof MaterialEsquadria]
        : value
))

const StatusAmbienteResponseSchema = z.union([
    z.enum(StatusAmbiente),
    z.enum(['PUBLICADO', 'NAO_PUBLICADO', 'AGUARDANDO_VALIDACAO']),
]).transform((value) => (
    value in StatusAmbiente
        ? StatusAmbiente[value as keyof typeof StatusAmbiente]
        : value
))

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
    tipo: TipoGeometriaResponseSchema,
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
    tipo: TipoEsquadriaResponseSchema,
    geometria: GeometriaEsquadriaSchema,
    alturaPeitoril: z.number(),
    area: z.number(),
    material: MaterialEsquadriaResponseSchema,
    informacaoAdicional: z.string(),
})

export type Esquadria = z.infer<typeof EsquadriaSchema>

// Resumo por tipo/material (EsquadriaTipoMaterialRes)
const EsquadriaTipoMaterialSchema = z.object({
    tipo: TipoEsquadriaResponseSchema,
    material: MaterialEsquadriaResponseSchema,
    area: z.number(),
})

export type EsquadriaTipoMaterial = z.infer<typeof EsquadriaTipoMaterialSchema>

// Detalhes de esquadrias (EsquadriasDetalhesRes)
const EsquadriasDetalhesSchema = z.object({
    esquadrias: z.array(EsquadriaSchema),
    esquadriasTipoMaterial: z.array(EsquadriaTipoMaterialSchema),
})

// Ambiente detalhado (AmbienteRes)
export const AmbienteDetalheSchema = AmbienteDetalheBaseSchema.extend({
    geometrias: z.array(GeometriaAmbienteSchema),
    areaAmbiente: z.number(),
    pesDireitos: z.array(z.number()),
    esquadriasDetalhes: EsquadriasDetalhesSchema,
    informacaoAdicional: z.string(),
    status: StatusAmbienteResponseSchema,
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

export type AmbienteEsquadrias = z.infer<typeof AmbienteEsquadriasSchema>

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
