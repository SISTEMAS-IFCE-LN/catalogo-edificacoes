import {z} from 'zod'
import {
    Bloco,
    MaterialEsquadria,
    StatusAmbiente,
    TipoAmbiente,
    TipoEsquadria,
    TipoGeometria, Unidade
} from '@/types/ambientes/enums'

export const LocalizacaoSchema = z.object({
    id: z.number(),
    bloco: z.enum(Bloco),
    unidade: z.enum(Unidade),
    andar: z.int(),
})

export const AmbienteBasicoSchema = z.object({
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
export const GeometriaAmbienteSchema = z.object({
    id: z.number(),
    tipo: z.enum(TipoGeometria),
    base: z.number(),
    altura: z.number(),
    repeticao: z.int(),
    area: z.number(),
})

// Geometria da esquadria (GeometriaEsquadriaRes)
export const GeometriaEsquadriaSchema = z.object({
    id: z.number(),
    base: z.number(),
    altura: z.number(),
    repeticao: z.int(),
    area: z.number(),
})

// Esquadria (EsquadriaRes)
export const EsquadriaSchema = z.object({
    id: z.number(),
    tipo: z.enum(TipoEsquadria),
    geometria: GeometriaEsquadriaSchema,
    alturaPeitoril: z.number(),
    area: z.number(),
    material: z.enum(MaterialEsquadria),
    informacaoAdicional: z.string(),
})

// Resumo por tipo/material (EsquadriaTipoMaterialRes)
export const EsquadriaTipoMaterialSchema = z.object({
    tipo: z.enum(TipoEsquadria),
    material: z.enum(MaterialEsquadria),
    area: z.number(),
})

// Detalhes de esquadrias (EsquadriasDetalhesRes)
export const EsquadriasDetalhesSchema = z.object({
    esquadrias: z.array(EsquadriaSchema),
    esquadriasTipoMaterial: z.array(EsquadriaTipoMaterialSchema),
})

// Ambiente detalhado (AmbienteRes)
export const AmbienteDetalheSchema = AmbienteBasicoSchema.extend({
    geometrias: z.array(GeometriaAmbienteSchema),
    areaAmbiente: z.number(),
    pesDireitos: z.array(z.number()),
    esquadriasDetalhes: EsquadriasDetalhesSchema,
    informacaoAdicional: z.string(),
    status: z.enum(StatusAmbiente),
})

export type AmbienteDetalhe = z.infer<typeof AmbienteDetalheSchema>