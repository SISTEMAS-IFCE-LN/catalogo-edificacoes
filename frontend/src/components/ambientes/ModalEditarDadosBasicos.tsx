import {ModalFormulario} from '@/components/ambientes/ModalFormulario'
import {CampoNumerico} from '@/components/ambientes/CampoNumerico'
import {ErroCampo} from '@/components/ambientes/ErroCampo'
import {LocalizacaoFields} from '@/components/ambientes/LocalizacaoFields'
import {atualizarDadosBasicos} from '@/lib/api/api-naopublicados'
import {dadosBasicosDeDetalhe} from '@/lib/ambientes/mappers'
import {dadosBasicosSchema} from '@/types/ambientes/request'
import type {AmbienteDetalhe} from '@/types/ambientes/response'
import {Input} from '@/components/ui/input'
import {Label} from '@/components/ui/label'

// Limite do backend (@Size(max=50) de AmbienteReq.kt)
const NOME_MAX = 50

interface ModalEditarDadosBasicosProps {
    open: boolean
    ambiente: AmbienteDetalhe
    onOpenChange: (open: boolean) => void
    onSalvou: () => void
}

// UC07-FE — PATCH /{id}/dados-basicos: nome + localizacao + capacidade.
export function ModalEditarDadosBasicos({
                                            open,
                                            ambiente,
                                            onOpenChange,
                                            onSalvou,
                                        }: ModalEditarDadosBasicosProps) {
    return (
        <ModalFormulario
            open={open}
            title="Editar Dados Básicos"
            schema={dadosBasicosSchema}
            defaults={dadosBasicosDeDetalhe(ambiente)}
            onOpenChange={onOpenChange}
            onSubmit={(values) => atualizarDadosBasicos(ambiente.id, values)}
            onSalvou={onSalvou}
            mensagemSucesso="Dados básicos atualizados."
            mensagemPadrao="Erro ao atualizar dados básicos."
        >
            {(form) => (
                <>
                    <div className="space-y-1.5">
                        <Label htmlFor="dados-basicos-nome">Nome</Label>
                        <Input id="dados-basicos-nome" maxLength={NOME_MAX} {...form.register('nome')}/>
                        {form.formState.errors.nome && (
                            <ErroCampo mensagem={String(form.formState.errors.nome.message)}/>
                        )}
                    </div>
                    <CampoNumerico
                        label="Capacidade"
                        id="dados-basicos-capacidade"
                        inputMode="numeric"
                        registration={form.register('capacidade', {valueAsNumber: true})}
                        erro={form.formState.errors.capacidade?.message}
                    />
                    <LocalizacaoFields form={form} idPrefix="dados-basicos"/>
                </>
            )}
        </ModalFormulario>
    )
}
