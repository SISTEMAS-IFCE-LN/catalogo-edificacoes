import {useId, type ReactNode} from 'react'
import {
    useForm,
    type DefaultValues,
    type Resolver,
    type UseFormReturn,
} from 'react-hook-form'
import {zodResolver} from '@hookform/resolvers/zod'
import type {FieldValues} from 'react-hook-form'
import type {z} from 'zod'
import {ResponsiveModal} from '@/components/common/ResponsiveModal'
import {Button} from '@/components/ui/button'
import {useAsyncAction} from '@/hooks/useAsyncAction'
import {toast} from 'sonner'

export interface ModalFormularioProps<V extends FieldValues, R = void> {
    open: boolean
    title: string
    description?: string
    schema: z.ZodType<V>
    defaults: V
    // Rejeita em erro → useAsyncAction toasta ErroRes.mensagem e o modal
    // permanece aberto (padrão da parte 09). O retorno chega ao onSalvou
    // (R = number nas UC16/UC17; void nas demais).
    onSubmit: (values: V) => Promise<R>
    onSalvou: (resultado: R) => void
    onOpenChange: (open: boolean) => void
    salvarLabel?: string
    mensagemSucesso?: string
    mensagemPadrao?: string
    children: (form: UseFormReturn<V>) => ReactNode
}

// Shell dos modais de edição (UC07–UC17): ResponsiveModal + RHF + zodResolver +
// useAsyncAction. A página monta o modal condicionalmente, então os
// `defaultValues` são relidos a cada abertura — sem reset em efeito (regra
// react-hooks/set-state-in-effect). O footer fica FORA do <form> (prop do
// ResponsiveModal): os botões submetem via o atributo form={formId}, mantendo
// header/footer fixos e apenas o corpo rolante (parte 12 §6–§7).
export function ModalFormulario<V extends FieldValues, R = void>({
                                                                     open,
                                                                     title,
                                                                     description,
                                                                     schema,
                                                                     defaults,
                                                                     onSubmit,
                                                                     onSalvou,
                                                                     onOpenChange,
                                                                     salvarLabel = 'Salvar',
                                                                     mensagemSucesso = 'Salvo com sucesso.',
                                                                     mensagemPadrao = 'Erro ao salvar. Tente novamente.',
                                                                     children,
                                                                 }: ModalFormularioProps<V, R>) {
    const formId = useId()

    // TTransformedValues = V: o handleSubmit entrega ao onValid o OUTPUT do
    // schema (valores parseados/transformados, ex.: trim, defaults).
    const form = useForm<V, unknown, V>({
        // O schema é genérico: os overloads do zodResolver não inferem o output
        // de V no corpo do componente — o cast alinha o contrato (parse do zod
        // + entrega dos valores transformados ao handleSubmit).
        resolver: zodResolver(schema as never) as unknown as Resolver<V, unknown, V>,
        // Cast necessário: TS não prova V → DeepPartial<V> para genérico aberto.
        defaultValues: defaults as DefaultValues<V>,
    })

    const {executando, executar} = useAsyncAction({
        onClose: () => onOpenChange(false),
        mensagemPadrao,
    })

    function salvar(values: V) {
        void executar(async () => {
            const resultado = await onSubmit(values)
            toast.success(mensagemSucesso)
            onSalvou(resultado)
        })
    }

    return (
        <ResponsiveModal
            open={open}
            onOpenChange={onOpenChange}
            title={title}
            description={description}
            footer={
                <>
                    <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                        Cancelar
                    </Button>
                    <Button type="submit" form={formId} disabled={executando}>
                        {executando ? 'Salvando…' : salvarLabel}
                    </Button>
                </>
            }
        >
            <form id={formId} onSubmit={form.handleSubmit(salvar)} className="space-y-3">
                {children(form)}
            </form>
        </ResponsiveModal>
    )
}
