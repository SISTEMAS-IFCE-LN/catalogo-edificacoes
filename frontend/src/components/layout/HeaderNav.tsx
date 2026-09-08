import { Link, useLocation } from 'react-router'
import { useAuth } from '@/hooks/useAuth'
import { cn } from '@/lib/utils/shadcn-helper'
import { Role } from '@/types/usuarios/user'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Button } from '@/components/ui/button'
import { Menu } from 'lucide-react'

interface MenuItem {
  href: string
  label: string
  roles: Role[] | null
}

const menuItens: MenuItem[] = [
  // Grupo "Ambientes"
  { href: '/ambientes/publicados', label: 'Listar', roles: null },
  { href: '/ambientes/validacao', label: 'Validar', roles: [Role.VALIDADOR] },
  { href: '/ambientes/nao-publicados', label: 'Gerir', roles: [Role.GESTOR_SISTEMA] },
  // Grupo "Usuários"
  { href: '/usuarios', label: 'Gerir', roles: [Role.ADMINISTRADOR] },
]

export function HeaderNav() {
  const { user } = useAuth()
  const location = useLocation()

  if (!user) return null

  const visibleItems = menuItens.filter((item) => {
    if (item.roles === null) return true
    return item.roles.some((r) => user.perfis.includes(r))
  })

  if (visibleItems.length === 0) return null

  const ambientesItems = visibleItems.filter((i) =>
    ['/ambientes/publicados', '/ambientes/validacao', '/ambientes/nao-publicados'].includes(i.href),
  )
  const usuariosItems = visibleItems.filter((i) => i.href === '/usuarios')

  return (
    <DropdownMenu>
      <DropdownMenuTrigger>
        <Button
          variant="ghost"
          size="sm"
          className="h-10 px-2 hover:bg-transparent"
          aria-label="Abrir menu de navegação"
        >
          <Menu className="h-5 w-5" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start" className="w-56">
        {ambientesItems.length > 0 && (
          <>
            <DropdownMenuGroup>
              <DropdownMenuLabel>Ambientes</DropdownMenuLabel>
              {ambientesItems.map((item) => (
                <DropdownMenuItem key={item.href}>
                  <Link
                    to={item.href}
                    className={cn(
                      'cursor-pointer flex items-center gap-2 py-2',
                      location.pathname === item.href && 'bg-accent text-accent-foreground font-medium',
                    )}
                  >
                    {item.label}
                  </Link>
                </DropdownMenuItem>
              ))}
            </DropdownMenuGroup>
            <DropdownMenuSeparator />
          </>
        )}
        {usuariosItems.length > 0 && (
          <DropdownMenuGroup>
            <DropdownMenuLabel>Usuários</DropdownMenuLabel>
            {usuariosItems.map((item) => (
              <DropdownMenuItem key={item.href}>
                <Link
                  to={item.href}
                  className={cn(
                    'cursor-pointer flex items-center gap-2 py-2',
                    location.pathname === item.href && 'bg-accent text-accent-foreground font-medium',
                  )}
                >
                  {item.label}
                </Link>
              </DropdownMenuItem>
            ))}
          </DropdownMenuGroup>
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
