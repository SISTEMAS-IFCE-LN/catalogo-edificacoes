import {z} from 'zod'

// Resposta dos endpoints de referência (CampusRes/UnidadeRes/BlocoRes)
export const ReferenciaSchema = z.object({
    id: z.number().int().positive(),
    nome: z.string(),
})
export const ReferenciaListSchema = z.array(ReferenciaSchema)

export type ReferenciaRes = z.infer<typeof ReferenciaSchema>

// Request de escrita: exatamente um dos dois (id existente OU nome novo).
// `nome` vazio é estado transitório do input; a validação de submit barra.
export const ReferenciaInputSchema = z.object({
    id: z.number().int().positive().nullable(),
    nome: z.string().trim().max(50, 'Máximo 50 caracteres.').nullable(),
}).refine((v) => (v.id !== null) !== (v.nome !== null && v.nome.length > 0), {
    message: 'Informe id ou nome (apenas um).',
})

export type ReferenciaInput = z.infer<typeof ReferenciaInputSchema>

export const REFERENCIA_VAZIA: ReferenciaInput = {id: null, nome: null}
