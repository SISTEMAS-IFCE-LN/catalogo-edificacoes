import {Navigate, Outlet, useLocation} from 'react-router'
import {useAuth} from '@/hooks/useAuth'
import {hasPermission} from '@/lib/security/permissions'
import type {Role} from '@/types/usuarios/user'

export function RequireRole({roles}: { roles: Role[] }) {
    const {user} = useAuth()
    const location = useLocation()

    if (!user) {
        return <Navigate to="/login" replace state={{ from: location }} />
    }

    if (!hasPermission(user.perfis, roles)) {
        return <Navigate to="/unauthorized" replace />
    }

    return <Outlet />
}