import { z } from 'zod'

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

export interface Filtros {
  nome: string
  bloco: string
  unidade: string
  andar: number | null
  tipo: string
}

export const FILTROS_VAZIOS: Filtros = {
  nome: '',
  bloco: '',
  unidade: '',
  andar: null,
  tipo: '',
}

// Schema para validar parâmetros da URL (evita NaN em page/size/andar)
// Usa preprocess para converter null em undefined (searchParams.get() retorna null quando não existe)
export const UrlFiltrosSchema = z.object({
  page: z.preprocess((val) => val === null ? undefined : val, z.coerce.number().int().nonnegative().default(0)),
  size: z.preprocess((val) => val === null ? undefined : val, z.coerce.number().int().positive().default(20)),
  nome: z.preprocess((val) => val === null ? undefined : val, z.string().default('')),
  bloco: z.preprocess((val) => val === null ? undefined : val, z.string().default('')),
  unidade: z.preprocess((val) => val === null ? undefined : val, z.string().default('')),
  andar: z.preprocess((val) => val === null ? undefined : val, z.coerce.number().int().nullable().default(null)),
  tipo: z.preprocess((val) => val === null ? undefined : val, z.string().default('')),
})

export type UrlFiltros = z.infer<typeof UrlFiltrosSchema>