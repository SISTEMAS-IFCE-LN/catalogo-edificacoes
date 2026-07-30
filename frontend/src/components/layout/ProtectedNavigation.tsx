import {NavLink} from 'react-router'
import {useAuth} from '@/hooks/useAuth'
import {cn} from '@/lib/utils'
import {Role} from '@/types/user'

interface MenuItem {
    href: string
    label: string
    roles: Role[] | null
}

const menuItens: MenuItem[] = [
    {href: '/ambientes/publicados', label: 'Publicados', roles: null},
    {href: '/ambientes/validacao', label: 'Aguardando Validação', roles: [Role.VALIDADOR]},
    {href: '/ambientes/nao-publicados', label: 'Não Publicados', roles: [Role.GESTOR_SISTEMA]},
    {href: '/usuarios', label: 'Usuários', roles: [Role.ADMINISTRADOR]},
]

export function ProtectedNavigation() {
    const {user} = useAuth()

    const visibleItems = menuItens.filter((item) => {
        if (!user) return false
        if (item.roles === null) return true
        return user.perfis.some((r) => item.roles!.includes(r))
    })

    return (
        <nav className="hidden md:flex items-center gap-1">
            {visibleItems.map((item) => (
                <NavLink
                    key={item.href}
                    to={item.href}
                    className={({isActive}) =>
                        cn(
                            'px-4 py-2 rounded-md text-sm font-medium transition-colors',
                            isActive
                                ? 'bg-primary text-primary-foreground'
                                : 'hover:bg-accent hover:text-accent-foreground',
                        )
                    }
                >
                    {item.label}
                </NavLink>
            ))}
        </nav>
    )
}