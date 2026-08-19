// Tipos de filtro da página /usuarios.
// Espelha o padrão de filtros de ambientes (types/ambientes/enums.ts + filtros.ts),
// porém simplificado: a busca de usuários só suporta nome ou email.

export enum TipoFiltroUsuarios {
    NENHUM = '',
    NOME = 'nome',
    EMAIL = 'email',
}

export interface FiltrosUsuarios {
    nome: string
    email: string
}

export const FILTROS_USUARIOS_VAZIOS: FiltrosUsuarios = {nome: '', email: ''}
