import type { Role } from '@/types/user'

export function hasPermission(
    userRoles: Role[],
    requiredRoles: Role[],
): boolean {
    return userRoles.some((role) => requiredRoles.includes(role))
}

export function matchRoute(pattern: string, pathname: string): boolean {
    const re = new RegExp('^' + pattern.replace(/:[^/]+/g, '[^/]+') + '$')
    return re.test(pathname)
}