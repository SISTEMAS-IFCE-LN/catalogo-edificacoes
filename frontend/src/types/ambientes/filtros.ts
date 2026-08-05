import { z } from 'zod'

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

// Validações alinhadas ao backend (BaseController.kt)
const NomeFiltroSchema = z.string()
    .max(50, 'Máximo 50 caracteres')

const TipoFiltroSchema = z.string()
    .max(50, 'Máximo 50 caracteres')

const BlocoFiltroSchema = z.string()
    .max(50, 'Máximo 50 caracteres')

const UnidadeFiltroSchema = z.string()
    .max(50, 'Máximo 50 caracteres')

const AndarFiltroSchema = z.coerce.number()
    .int()
    .min(0, 'Andar deve ser maior ou igual a 0')
    .nullable()

// Schema para validar parâmetros da URL (evita NaN em page/size/andar)
// Usa preprocess para converter null em undefined (searchParams.get() retorna null quando não existe)
// Para URL, campos opcionais podem ser vazios
export const UrlFiltrosSchema = z.object({
    page: z.preprocess((val) => val === null ? undefined : val, z.coerce.number().int().nonnegative().default(0)),
    size: z.preprocess((val) => val === null ? undefined : val, z.coerce.number().int().positive().default(20)),
    nome: z.preprocess((val) => val === null ? undefined : val, NomeFiltroSchema.default('')),
    tipo: z.preprocess((val) => val === null ? undefined : val, TipoFiltroSchema.default('')),
    bloco: z.preprocess((val) => val === null ? undefined : val, BlocoFiltroSchema.default('')),
    unidade: z.preprocess((val) => val === null ? undefined : val, UnidadeFiltroSchema.default('')),
    andar: z.preprocess((val) => val === null ? undefined : val, AndarFiltroSchema.default(null)),
})
