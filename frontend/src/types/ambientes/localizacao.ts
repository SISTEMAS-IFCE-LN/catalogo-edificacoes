import {z} from 'zod'
import {ReferenciaSchema} from '@/types/ambientes/referencia'

// Resposta de localização (LocalizacaoRes): campus/unidade/bloco aninhados
// espelham a hierarquia e permitem exibir/editar os três níveis.
export const LocalizacaoSchema = z.object({
    id: z.number(),
    campus: ReferenciaSchema,
    unidade: ReferenciaSchema,
    bloco: ReferenciaSchema,
    andar: z.int(),
})
