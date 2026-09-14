import {useWatch, type UseFormReturn} from 'react-hook-form'
import {z} from 'zod'
import {PlusIcon} from 'lucide-react'
import {ModalFormulario} from '@/components/ambientes/ModalFormulario'
import {CampoNumerico} from '@/components/ambientes/CampoNumerico'
import {BotaoRemover} from '@/components/ambientes/BotaoRemover'
import {ErroCampo} from '@/components/ambientes/ErroCampo'
import {Button} from '@/components/ui/button'

const listaPesDireitosSchema = z.object({
    pesDireitos: z.array(z.number().positive('Informe um valor maior que 0.'))
        .min(1, 'Pelo menos um pé-direito'),
})
type ListaPesDireitos = z.infer<typeof listaPesDireitosSchema>

interface ModalPesDireitosProps {
    open: boolean
    modo: 'incluir' | 'editar'
    titulo: string
    inicial: number[]
    onOpenChange: (open: boolean) => void
    onSubmit: (pesDireitos: number[]) => Promise<void>
    onSalvou: () => void
}

// Genérico das UC10-FE (incluir) e UC11-FE (editar).
export function ModalPesDireitos({
                                      open,
                                      modo,
                                      titulo,
                                      inicial,
                                      onOpenChange,
                                      onSubmit,
                                      onSalvou,
                                  }: ModalPesDireitosProps) {
    return (
        <ModalFormulario
            open={open}
            title={titulo}
            schema={listaPesDireitosSchema}
            defaults={{pesDireitos: inicial.length > 0 ? inicial : [0]}}
            onOpenChange={onOpenChange}
            onSubmit={async ({pesDireitos}) => onSubmit(pesDireitos)}
            onSalvou={onSalvou}
            mensagemSucesso={modo === 'incluir' ? 'Pés-direitos incluídos.' : 'Pés-direitos atualizados.'}
            mensagemPadrao={`Erro ao ${modo === 'incluir' ? 'incluir' : 'atualizar'} pés-direitos.`}
        >
            {(form) => <ListaPesDireitos form={form}/>}
        </ModalFormulario>
    )
}

// `useFieldArray` exige arrays de objetos; `pesDireitos` é `number[]` (contrato
// do backend), então a lista é gerenciada com useWatch/setValue — mesmo padrão
// da etapa de pés-direitos do FormAmbiente.
function ListaPesDireitos({form}: { form: UseFormReturn<ListaPesDireitos> }) {
    const {register, control, formState: {errors}, setValue} = form
    const alturas = useWatch({control, name: 'pesDireitos'}) ?? []

    function adicionar() {
        setValue('pesDireitos', [...alturas, 0], {shouldDirty: true})
    }

    function remover(indice: number) {
        setValue('pesDireitos', alturas.filter((_, i) => i !== indice), {shouldDirty: true})
    }

    return (
        <div className="space-y-3">
            {alturas.map((_, i) => (
                <div key={i} className="flex items-end gap-2">
                    <div className="flex-1">
                        <CampoNumerico
                            label={`Pé-direito ${i + 1} (m)`}
                            id={`pesDireitos.${i}`}
                            step="0.01"
                            registration={register(`pesDireitos.${i}`, {valueAsNumber: true})}
                            erro={errors.pesDireitos?.[i]?.message}
                        />
                    </div>
                    <BotaoRemover ariaLabel={`Remover pé-direito ${i + 1}`} onClick={() => remover(i)}/>
                </div>
            ))}
            <Button type="button" variant="outline" onClick={adicionar}>
                <PlusIcon/> Adicionar pé-direito
            </Button>
            {errors.pesDireitos?.root && <ErroCampo mensagem={String(errors.pesDireitos.root.message)}/>}
        </div>
    )
}
