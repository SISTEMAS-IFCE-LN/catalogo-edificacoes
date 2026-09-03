import {ModalFormulario} from '@/components/ambientes/ModalFormulario'
import {ErroCampo} from '@/components/ambientes/ErroCampo'
import {LocalizacaoFields} from '@/components/ambientes/LocalizacaoFields'
import {duplicarAmbiente} from '@/lib/api/api-naopublicados'
import {duplicacaoDeDetalhe} from '@/lib/ambientes/mappers'
import {duplicacaoSchema} from '@/types/ambientes/request'
import type {AmbienteDetalhe} from '@/types/ambientes/response'
import {Input} from '@/components/ui/input'
import {Label} from '@/components/ui/label'

// Limite do backend (@Size(max=50) de AmbienteNomeLocalizacaoReq.kt)
const NOME_MAX = 50

interface Props {
    open: boolean
    ambiente: AmbienteDetalhe
    onOpenChange: (open: boolean) => void
    // A página navega para o novo registro (casos-uso UC17-FE: "redirecionar
    // para DetalheAmbiente do novo registro").
    onSalvou: (novoId: number) => void
}

// UC17-FE — POST /{id}/duplicar com nome/localizacao editáveis, pré-preenchidos
// com os dados ORIGINAIS (SEM sufixo "(cópia)"). Em erro (RN-1.7, duplicar 2×)
// o ErroRes.mensagem é tostado e o modal permanece aberto para correção.
export function ModalDuplicar({open, ambiente, onOpenChange, onSalvou}: Props) {
    return (
        <ModalFormulario
            open={open}
            title="Duplicar Ambiente"
            description="Defina o nome e a localização do novo ambiente."
            schema={duplicacaoSchema}
            defaults={duplicacaoDeDetalhe(ambiente)}
            onOpenChange={onOpenChange}
            onSubmit={async (values) => (await duplicarAmbiente(ambiente.id, values)).id}
            onSalvou={onSalvou}
            salvarLabel="Duplicar"
            mensagemSucesso="Ambiente duplicado."
            mensagemPadrao="Erro ao duplicar ambiente."
        >
            {(form) => (
                <>
                    <div className="space-y-1.5">
                        <Label htmlFor="duplicar-nome">Nome</Label>
                        <Input id="duplicar-nome" maxLength={NOME_MAX} {...form.register('nome')}/>
                        {form.formState.errors.nome && (
                            <ErroCampo mensagem={String(form.formState.errors.nome.message)}/>
                        )}
                    </div>
                    <LocalizacaoFields form={form} idPrefix="duplicar"/>
                </>
            )}
        </ModalFormulario>
    )
}
