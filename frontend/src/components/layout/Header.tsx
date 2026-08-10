import {Link} from 'react-router'
import {ProtectedNavigation} from '@/components/layout/ProtectedNavigation'
import {useAuth} from '@/hooks/useAuth'
import {Button, buttonVariants} from '@/components/ui/button'
import {ROUTES} from '@/constants/routes'
import {cn} from '@/lib/utils'

export function Header() {
    const {user, logout} = useAuth()

    return (
        <header className="border-b border-border bg-background">
            <div className="container mx-auto px-4">
                <div className="flex h-16 items-center justify-between">
                    <Link to="/" className="font-bold text-xl">Catálogo Edificações</Link>
                    {user && <ProtectedNavigation/>}
                    <div className="flex items-center gap-4">
                        {user ? (
                            <>
                                <span className="text-sm hidden md:block">
                                    <span className="font-medium">{user.nome}</span>
                                    <br/>
                                    <span className="text-muted-foreground text-xs">{user.email}</span>
                                </span>
                                <Button variant="outline" size="sm" onClick={() => logout()}>
                                    Sair
                                </Button>
                            </>
                        ) : (
                            <Link
                                to={ROUTES.LOGIN}
                                className={cn(buttonVariants({variant: 'default', size: 'sm'}))}
                            >
                                Login
                            </Link>
                        )}
                    </div>
                </div>
            </div>
        </header>
    )
}