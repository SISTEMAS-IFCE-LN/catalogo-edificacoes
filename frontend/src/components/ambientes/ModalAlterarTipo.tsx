import {ResponsiveModal} from '@/components/common/ResponsiveModal'
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
        <ResponsiveModal
            open={open}
            onOpenChange={onOpenChange}
            title="Alterar Tipo"
            description="Atenção: alterar o tipo cria um novo ambiente e remove o antigo."
        >
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
        </ResponsiveModal>
    )
}
