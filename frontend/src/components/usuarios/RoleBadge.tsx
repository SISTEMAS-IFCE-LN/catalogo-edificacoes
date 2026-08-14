import {Badge} from '@/components/ui/badge'
import {Role} from '@/types/user'
import {ROLE_LABELS} from '@/constants/roles'

export function RoleBadge({role}: { role: Role }) {
    return <Badge variant="secondary">{ROLE_LABELS[role]}</Badge>
}