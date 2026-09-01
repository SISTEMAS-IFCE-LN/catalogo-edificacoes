import {Button} from '@/components/ui/button'
import {Card} from '@/components/ui/card'
import {PAGES_ROUTES} from '@/constants/routes'

export function LoginPage() {

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