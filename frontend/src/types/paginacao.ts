import {z} from 'zod'

export const DadosPaginacaoSchema = z.object({
    totalElements: z.int(),
    totalPages: z.int(),
    currentPage: z.int(),
    pageSize: z.int(),
    hasNext: z.boolean(),
    hasPrevious: z.boolean(),
})

export type DadosPaginacao = z.infer<typeof DadosPaginacaoSchema>
