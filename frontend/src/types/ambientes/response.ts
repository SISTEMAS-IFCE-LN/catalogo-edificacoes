import {z} from 'zod'
import {TipoAmbiente} from '@/types/ambientes/enums'
import {
    StatusAmbienteResponseSchema,
    TipoGeometriaResponseSchema,
} from '@/types/ambientes/enums'
import {LocalizacaoSchema} from '@/types/ambientes/localizacao'
import {EsquadriasDetalhesSchema} from '@/types/ambientes/esquadrias'
import {DadosPaginacaoSchema} from '@/types/paginacao'

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

export const AmbientesBasicosPaginadosSchema = z.object({
    ambientes: z.array(AmbienteBasicoSchema),
    areaTotal: z.number(),
    dadosPaginacao: DadosPaginacaoSchema,
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
