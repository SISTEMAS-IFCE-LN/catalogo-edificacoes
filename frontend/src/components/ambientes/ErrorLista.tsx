import { Button } from '@/components/ui/button'

interface ErrorListaProps {
    onTentarNovamente: () => void
}

/**
 * Bloco de erro das listas de ambientes com ação "Tentar novamente"
 * (refetch). Compartilhado por publicados, validação e não-publicados.
 */
export function ErrorLista({ onTentarNovamente }: ErrorListaProps) {
    return (
        <div role="alert" className="border border-destructive rounded-lg p-4">
            <p className="text-destructive font-medium">Erro ao carregar ambientes.</p>
            <Button variant="outline" onClick={onTentarNovamente} className="mt-2">
                Tentar novamente
            </Button>
        </div>
    )
}
