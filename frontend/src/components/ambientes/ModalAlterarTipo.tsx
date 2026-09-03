import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog'
import {FormAmbiente} from '@/components/ambientes/FormAmbiente'
import {useAsyncAction} from '@/hooks/useAsyncAction'
import {alterarTipo} from '@/lib/api/api-naopublicados'
import {ambienteDeDetalhe} from '@/lib/ambientes/mappers'
import type {AmbienteDetalhe} from '@/types/ambientes/response'
import {toast} from 'sonner'

interface ModalAlterarTipoProps {
    open: boolean
    ambiente: AmbienteDetalhe
    onOpenChange: (open: boolean) => void
    // A página navega para o novo registro (casos-uso UC16-FE: "redirecionado
    // para DetalheAmbiente do novo registro") — o ambiente antigo deixa de existir.
    onSalvou: (novoId: number) => void
}

// UC16-FE — POST /{id} recebe um AmbienteReq COMPLETO (não apenas o tipo):
// o wizard é pré-preenchido com todos os dados atuais (conversão rótulo →
// nome técnico via mappers) e o usuário altera o que precisar. Alterar o tipo
// CRIA um novo ambiente e REMOVE o antigo — daí o alerta na descrição.
export function ModalAlterarTipo({open, ambiente, onOpenChange, onSalvou}: ModalAlterarTipoProps) {
    const {executando, executar} = useAsyncAction({
        onClose: () => onOpenChange(false),
        mensagemPadrao: 'Erro ao alterar tipo.',
    })

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-h-[85dvh] overflow-y-auto">
                <DialogHeader>
                    <DialogTitle>Alterar Tipo</DialogTitle>
                    <DialogDescription>
                        Atenção: alterar o tipo cria um novo ambiente e remove o antigo.
                    </DialogDescription>
                </DialogHeader>
                <FormAmbiente
                    initial={ambienteDeDetalhe(ambiente)}
                    onSubmit={(values) =>
                        executar(async () => {
                            const novo = await alterarTipo(ambiente.id, values)
                            toast.success('Tipo alterado.')
                            onSalvou(novo.id)
                        })
                    }
                />
                {executando && <p className="text-sm text-muted-foreground">Salvando…</p>}
            </DialogContent>
        </Dialog>
    )
}
