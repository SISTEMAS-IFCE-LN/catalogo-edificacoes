import {ModalFormulario} from '@/components/ambientes/ModalFormulario'
import {ErroCampo} from '@/components/ambientes/ErroCampo'
import {atualizarInfoAdicional} from '@/lib/api/api-naopublicados'
import {informacaoAdicionalSchema} from '@/types/ambientes/request'
import type {AmbienteDetalhe} from '@/types/ambientes/response'
import type {UseFormReturn} from 'react-hook-form'
import {Label} from '@/components/ui/label'
import {Textarea} from '@/components/ui/textarea'

// Máx. de caracteres da informação adicional do ambiente (@Size(max=255))
const INFO_ADICIONAL_MAX = 255

interface ModalInfoAdicionalProps {
    open: boolean
    ambiente: AmbienteDetalhe
    onOpenChange: (open: boolean) => void
    onSalvou: () => void
}

// UC14-FE — PATCH /{id}/informacao-adicional. O corpo text/plain (string CRUA,
// nunca objeto) é montado em api-naopublicados.atualizarInfoAdicional.
export function ModalInfoAdicional({open, ambiente, onOpenChange, onSalvou}: ModalInfoAdicionalProps) {
    return (
        <ModalFormulario
            open={open}
            title="Informação Adicional"
            description="Informação complementar do ambiente (opcional)."
            schema={informacaoAdicionalSchema}
            defaults={{informacaoAdicional: ambiente.informacaoAdicional}}
            onOpenChange={onOpenChange}
            onSubmit={({informacaoAdicional}) => atualizarInfoAdicional(ambiente.id, informacaoAdicional)}
            onSalvou={onSalvou}
            mensagemSucesso="Informação adicional atualizada."
            mensagemPadrao="Erro ao atualizar informação adicional."
        >
            {(form) => <CampoInfoAdicional form={form}/>}
        </ModalFormulario>
    )
}

function CampoInfoAdicional({form}: { form: UseFormReturn<{ informacaoAdicional: string }> }) {
    // O campo é opcional: vazio passa e o max(255) só atua quando preenchido.
    const valor = form.watch('informacaoAdicional') ?? ''

    return (
        <div className="space-y-1.5">
            <Label htmlFor="info-adicional">Informação Adicional (opcional)</Label>
            <Textarea
                id="info-adicional"
                rows={4}
                maxLength={INFO_ADICIONAL_MAX}
                placeholder="Ex.: sala com ar-condicionado"
                {...form.register('informacaoAdicional')}
            />
            <p className="text-right text-xs text-muted-foreground">
                {valor.length}/{INFO_ADICIONAL_MAX}
            </p>
            {form.formState.errors.informacaoAdicional && (
                <ErroCampo mensagem={String(form.formState.errors.informacaoAdicional.message)}/>
            )}
        </div>
    )
}
