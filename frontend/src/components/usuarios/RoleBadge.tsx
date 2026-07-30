import {Badge} from '@/components/ui/badge'
import {Role} from '@/types/user'

const LABELS: Record<Role, string> = {
    [Role.COLABORADOR]: 'Colaborador',
    [Role.VALIDADOR]: 'Validador',
    [Role.GESTOR_SISTEMA]: 'Gestor',
    [Role.ADMINISTRADOR]: 'Administrador',
}

export function RoleBadge({role}: { role: Role }) {
    return <Badge variant="secondary">{LABELS[role]}</Badge>
}