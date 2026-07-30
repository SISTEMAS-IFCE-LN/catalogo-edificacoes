import {Link} from 'react-router'
import {buttonVariants} from '@/components/ui/button'
import {ROUTES} from '@/constants/routes'

export function UnauthorizedPage() {
    return (
        <div className="min-h-screen flex flex-col items-center justify-center gap-4 p-4 text-center">
            <h1 className="text-2xl font-bold">Acesso negado</h1>
            <p className="text-muted-foreground">
                Você não tem permissão para acessar esta página.
            </p>
            <Link to={ROUTES.HOME} className={buttonVariants({variant: 'default'})}>
                Ir para a página inicial
            </Link>
        </div>
    )
}