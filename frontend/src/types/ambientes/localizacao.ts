import {z} from 'zod'
import {Bloco, Unidade} from '@/types/ambientes/enums'

export const LocalizacaoSchema = z.object({
    id: z.number(),
    bloco: z.enum(Bloco),
    unidade: z.enum(Unidade),
    andar: z.int(),
})
