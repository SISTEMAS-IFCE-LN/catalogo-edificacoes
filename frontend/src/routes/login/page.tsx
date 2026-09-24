import {useSearchParams} from 'react-router'
import {Button} from '@/components/ui/button'
import {Card} from '@/components/ui/card'
import {PAGES_ROUTES} from '@/constants/routes'

const MENSAGENS_ERRO: Record<string, string> = {
    inativo: 'Usuário inativo. Contate o administrador.',
    dominio: 'Acesso negado. Domínio não autorizado.',
    falha: 'Falha ao autenticar. Tente novamente.',
}

export function LoginPage() {
    const [searchParams] = useSearchParams()
    const erro = searchParams.get('erro')
    const mensagemErro = erro ? (MENSAGENS_ERRO[erro] ?? MENSAGENS_ERRO.falha) : null

    function handleGoogleLogin() {
        window.location.href = PAGES_ROUTES.GOOGLE_OAUTH_ENTRY
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-muted p-4">
            <Card className="p-8 max-w-md w-full space-y-6">
                <div className="space-y-2 text-center">
                    <h1 className="text-2xl font-bold">Catálogo de Edificações</h1>
                    <p className="text-sm text-muted-foreground">
                        IFCE — Campus Limoeiro do Norte
                    </p>
                </div>
                {mensagemErro && (
                    <p role="alert" className="text-sm text-destructive text-center">
                        {mensagemErro}
                    </p>
                )}
                <Button
                    className="w-full"
                    size="lg"
                    onClick={handleGoogleLogin}
                >
                    Entrar com Google
                </Button>
            </Card>
        </div>
    )
}