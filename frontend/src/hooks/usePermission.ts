import {useAuth} from "@/hooks/useAuth"
import {
    ACTION_PERMISSIONS,
    ROUTE_PERMISSIONS,
    hasPermission,
    matchRoute,
} from '@/lib/permissions'
import type {Role} from '@/types/user'

export function usePermission() {
    const {user} = useAuth()

    function canDo(action: string): boolean {
        if (!user) return false
        const required = ACTION_PERMISSIONS[action]
        if (!required) {
            console.warn(`Ação "${action}" não definida em ACTION_PERMISSIONS`)
            return false
        }
        return hasPermission(user.perfis, required)
    }

    function canAccess(pathname: string): boolean {
        if (!user) return false
        const entry = Object.entries(ROUTE_PERMISSIONS)
            .find(([pattern]) => matchRoute(pattern, pathname))
        if (!entry) return true
        const [, required] = entry
        return hasPermission(user.perfis, required)
    }

    function hasRole(roles: Role[]): boolean {
        if (!user) return false
        return hasPermission(user.perfis, roles)
    }

    return {canDo, canAccess, hasRole}
}