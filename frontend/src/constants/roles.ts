import {Role} from '@/types/user'

 export const ROLE_LABELS: Record<Role, string> = {
    [Role.COLABORADOR]: 'Colaborador',
    [Role.VALIDADOR]: 'Validador',
    [Role.GESTOR_SISTEMA]: 'Gestor do Sistema',
    [Role.ADMINISTRADOR]: 'Administrador',
}