import {z} from 'zod'

export const LocalizacaoBasicaSchema = z.object({
    id: z.number(),
    bloco: z.string(),
    unidade: z.string(),
    andar: z.number(),
})

export const AmbienteBasicoSchema = z.object({
    id: z.number(),
    nome: z.string(),
    tipo: z.string(),
    localizacao: LocalizacaoBasicaSchema,
    capacidade: z.number(),
    area: z.number(),
})

export type AmbienteBasico = z.infer<typeof AmbienteBasicoSchema>

export const AmbientesBasicosPaginadosSchema = z.object({
    ambientes: z.array(AmbienteBasicoSchema),
    areaTotal: z.number(),
    dadosPaginacao: z.object({
        totalElements: z.number(),
        totalPages: z.number(),
        currentPage: z.number(),
        pageSize: z.number(),
        hasNext: z.boolean(),
        hasPrevious: z.boolean(),
    }),
})

export type AmbientesBasicosPaginados = z.infer<typeof AmbientesBasicosPaginadosSchema>