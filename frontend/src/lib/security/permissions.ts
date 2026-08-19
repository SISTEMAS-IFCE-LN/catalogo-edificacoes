import {Role} from "@/types/usuarios/user";

export const ROUTE_PERMISSIONS: Record<string, Role[]> = {
    '/ambientes/validacao': [Role.VALIDADOR],
    '/ambientes/nao-publicados': [Role.GESTOR_SISTEMA],
    '/ambientes/publicados/:id': [Role.COLABORADOR],
    '/ambientes/publicados/esquadrias': [Role.COLABORADOR],
    '/usuarios': [Role.ADMINISTRADOR],
}

export const ACTION_PERMISSIONS: Record<string, Role[]> = {
    'ambiente:publicar': [Role.VALIDADOR],
    'ambiente:privar': [Role.VALIDADOR],

    'ambiente:criar': [Role.GESTOR_SISTEMA],
    'ambiente:editar': [Role.GESTOR_SISTEMA],
    'ambiente:deletar': [Role.GESTOR_SISTEMA],
    'ambiente:enviar-validacao': [Role.GESTOR_SISTEMA],
    'ambiente:alterar-tipo': [Role.GESTOR_SISTEMA],
    'ambiente:duplicar': [Role.GESTOR_SISTEMA],

    'ambiente:ver-esquadrias': [Role.COLABORADOR],

    'usuario:editar-perfis': [Role.ADMINISTRADOR],
    'usuario:desativar': [Role.ADMINISTRADOR],
    'usuario:ativar': [Role.ADMINISTRADOR],
}

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

export function getRequiredRoles(pathname: string): Role[] | null {
    const matchedRoute = Object.keys(ROUTE_PERMISSIONS)
        .filter((route) => matchRoute(route, pathname) || pathname.startsWith(route))
        .sort((a, b) => b.length - a.length)[0]
    return matchedRoute ? ROUTE_PERMISSIONS[matchedRoute] : null
}