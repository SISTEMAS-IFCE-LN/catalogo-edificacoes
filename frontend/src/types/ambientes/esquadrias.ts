import {z} from 'zod'
import {
    MaterialEsquadriaResponseSchema,
    TipoEsquadriaResponseSchema,
} from '@/types/ambientes/enums'
import {LocalizacaoSchema} from '@/types/ambientes/localizacao'
import {DadosPaginacaoSchema} from '@/types/paginacao'

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
export const EsquadriasDetalhesSchema = z.object({
    esquadrias: z.array(EsquadriaSchema),
    esquadriasTipoMaterial: z.array(EsquadriaTipoMaterialSchema),
})

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
    dadosPaginacao: DadosPaginacaoSchema,
})

export type EsquadriasResponse = z.infer<typeof EsquadriasResponseSchema>
