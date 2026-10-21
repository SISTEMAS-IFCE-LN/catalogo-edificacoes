import type {TipoFiltro} from '@/types/ambientes/enums'

// Query parameters para listagem de ambientes
export interface AmbientesQuery {
    page?: number
    size?: number
    nome?: string
    campusId?: number
    unidadeId?: number
    blocoId?: number
    andar?: number
    tipo?: string
    tipoFiltro: TipoFiltro
}

// Query parameters para UC20-FE
export interface EsquadriasQuery {
    ids: number[]
    page?: number
    size?: number
}
