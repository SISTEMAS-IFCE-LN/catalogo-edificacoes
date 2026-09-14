import {z} from 'zod'
import {
    Bloco,
    MaterialEsquadria,
    TipoAmbiente,
    TipoEsquadria,
    TipoGeometria,
    Unidade,
} from '@/types/ambientes/enums'

// As chaves dos enums TS espelham os NOMES TÉCNICOS dos enums Kotlin
// (SALA_AULA, BLOCO_1, PORTA...). O backend desserializa enums pelo NOME —
// nunca envie os rótulos ('Sala de Aula', 'Bloco 1'): daria 400.
// (arquitetura-frontend.md §13; ver também plano 07 §11)
const keysOf = <T extends Record<string, string>>(e: T) =>
    Object.keys(e) as [(keyof T & string), ...(keyof T & string)[]]

// Rótulo (resposta) → nome técnico (request). Necessário porque os DTOs de
// resposta devolvem rótulos (AmbienteRes.tipo = tipo.nome; LocalizacaoRes.bloco
// = bloco.nome), enquanto os DTOs de request exigem nomes técnicos.
export function nomeTecnicoDeRotulo<T extends Record<string, string>>(e: T, rotulo: string): string {
    const chave = Object.keys(e).find((k) => e[k] === rotulo)
    if (!chave) throw new Error(`Rótulo desconhecido: ${rotulo}`)
    return chave
}

// Espelha LocalizacaoReq do backend (NÃO tem `id`)
export const localizacaoSchema = z.object({
    bloco: z.enum(keysOf(Bloco)),          // 'BLOCO_1', ... (não 'Bloco 1')
    unidade: z.enum(keysOf(Unidade)),      // 'SEDE', ...
    andar: z.int().min(0),                 // backend @Min(0)
})
export type LocalizacaoInput = z.infer<typeof localizacaoSchema>

// Espelha AmbienteNomeLocalizacaoReq do backend (duplicação UC17 — nome com
// @NotBlank/@Size(max=50) + localizacao). O usuário define nome e localização
// do novo ambiente; a duplicidade (RN-1.7) é validada pelo backend.
export const duplicacaoSchema = z.object({
    nome: z.string().trim().min(1, 'Nome obrigatório.').max(50, 'Máximo 50 caracteres.'),
    localizacao: localizacaoSchema,
})
export type DuplicacaoInput = z.infer<typeof duplicacaoSchema>

// Espelha o payload de PATCH /{id}/dados-basicos (UC07-FE): nome (@NotBlank/
// @Size(max=50)) + localizacao + capacidade. Nome com trim — o payload enviado
// é o output do parse (zodResolver entrega os valores transformados).
export const dadosBasicosSchema = z.object({
    nome: z.string().trim().min(1, 'Nome obrigatório.').max(50, 'Máximo 50 caracteres.'),
    capacidade: z.int().positive('Capacidade deve ser maior que 0.'),
    localizacao: localizacaoSchema,
})
export type DadosBasicosInput = z.infer<typeof dadosBasicosSchema>

// Espelha o payload de PATCH /{id}/informacao-adicional (UC14-FE) —
// @Size(max=255) de AmbienteReq.kt. O corpo text/plain é montado na camada de
// API (api-naopublicados.atualizarInfoAdicional); aqui só se valida o texto.
export const informacaoAdicionalSchema = z.object({
    informacaoAdicional: z.string().max(255, 'Máximo 255 caracteres.'),
})
export type InformacaoAdicionalInput = z.infer<typeof informacaoAdicionalSchema>

// Espelha GeometriaAmbienteReq do backend
export const geometriaSchema = z.object({
    tipo: z.enum(keysOf(TipoGeometria)),   // 'RETANGULAR', 'TRIANGULAR'
    base: z.number().positive(),
    altura: z.number().positive(),
    repeticao: z.int().positive().default(1),
})
export type GeometriaInput = z.infer<typeof geometriaSchema>

// Espelha GeometriaEsquadriaReq do backend
const geometriaEsquadriaSchema = z.object({
    base: z.number().positive(),
    altura: z.number().positive(),
    repeticao: z.int().positive().default(1),
})

// Espelha EsquadriaReq do backend
export const esquadriaSchema = z.object({
    tipo: z.enum(keysOf(TipoEsquadria)),       // 'PORTA', 'JANELA', ...
    geometria: geometriaEsquadriaSchema,
    material: z.enum(keysOf(MaterialEsquadria)), // 'ALUMINIO', ...
    alturaPeitoril: z.number().min(0).default(0),
    informacaoAdicional: z.string().max(255).optional().default(''),
})
export type EsquadriaInput = z.infer<typeof esquadriaSchema>

// Espelha AmbienteReq do backend
export const ambienteSchema = z.object({
    nome: z.string().min(1).max(50),
    localizacao: localizacaoSchema,
    tipo: z.enum(keysOf(TipoAmbiente)),     // 'SALA_AULA', ...
    capacidade: z.int().positive(),
    geometrias: z.array(geometriaSchema).min(1, 'Pelo menos uma geometria'),
    pesDireitos: z.array(z.number().positive()).min(1, 'Pelo menos um pé-direito'),
    esquadrias: z.array(esquadriaSchema).refine(
        (arr) => arr.some((e) => e.tipo === 'PORTA'),
        'Pelo menos uma porta é obrigatória',
    ),
    informacaoAdicional: z.string().max(255).optional().default(''),
})

export type AmbienteInput = z.infer<typeof ambienteSchema>
