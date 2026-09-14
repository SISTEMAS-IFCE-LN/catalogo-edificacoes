import {Controller, useFieldArray, type UseFormReturn} from 'react-hook-form'
import {z} from 'zod'
import {PlusIcon} from 'lucide-react'
import {ModalFormulario} from '@/components/ambientes/ModalFormulario'
import {CampoEnum} from '@/components/ambientes/CampoEnum'
import {CampoNumerico} from '@/components/ambientes/CampoNumerico'
import {BotaoRemover} from '@/components/ambientes/BotaoRemover'
import {ErroCampo} from '@/components/ambientes/ErroCampo'
import {Button} from '@/components/ui/button'
import {Input} from '@/components/ui/input'
import {Label} from '@/components/ui/label'
import {esquadriaSchema, type EsquadriaInput} from '@/types/ambientes/request'
import {MaterialEsquadria, TipoEsquadria} from '@/types/ambientes/enums'

// Máx. de caracteres da info adicional da esquadria (@Size(max=255) de EsquadriaReq.kt)
const INFO_ADICIONAL_MAX = 255

const listaEsquadriasSchema = z.object({
    esquadrias: z.array(esquadriaSchema).min(1, 'Pelo menos uma esquadria'),
})
type ListaEsquadrias = z.infer<typeof listaEsquadriasSchema>

const ESQUADRIA_VAZIA: EsquadriaInput = {
    tipo: 'PORTA',
    geometria: {base: 0, altura: 0, repeticao: 1},
    material: 'NAO_SE_APLICA',
    alturaPeitoril: 0,
    informacaoAdicional: '',
}

interface ModalEsquadriasProps {
    open: boolean
    modo: 'incluir' | 'editar'
    titulo: string
    // modo editar: pré-preenchimento já convertido para nomes técnicos
    // (lib/ambientes/mappers.esquadriasDeDetalhe)
    inicial: EsquadriaInput[]
    onOpenChange: (open: boolean) => void
    onSubmit: (esquadrias: EsquadriaInput[]) => Promise<void>
    onSalvou: () => void
}

// Genérico das UC12-FE (incluir) e UC13-FE (editar).
export function ModalEsquadrias({
                                    open,
                                    modo,
                                    titulo,
                                    inicial,
                                    onOpenChange,
                                    onSubmit,
                                    onSalvou,
                                }: ModalEsquadriasProps) {
    return (
        <ModalFormulario
            open={open}
            title={titulo}
            schema={listaEsquadriasSchema}
            defaults={{esquadrias: inicial.length > 0 ? inicial : [{...ESQUADRIA_VAZIA, geometria: {...ESQUADRIA_VAZIA.geometria}}]}}
            onOpenChange={onOpenChange}
            onSubmit={async ({esquadrias}) => onSubmit(esquadrias)}
            onSalvou={onSalvou}
            mensagemSucesso={modo === 'incluir' ? 'Esquadrias incluídas.' : 'Esquadrias atualizadas.'}
            mensagemPadrao={`Erro ao ${modo === 'incluir' ? 'incluir' : 'atualizar'} esquadrias.`}
        >
            {(form) => <ListaEsquadrias form={form}/>}
        </ModalFormulario>
    )
}

function ListaEsquadrias({form}: { form: UseFormReturn<ListaEsquadrias> }) {
    const {register, control, formState: {errors}} = form
    const {fields, append, remove} = useFieldArray({control, name: 'esquadrias'})

    return (
        <div className="space-y-3">
            {fields.map((item, i) => (
                <div key={item.id} className="space-y-3 rounded-lg border p-4">
                    <div className="flex items-center justify-between">
                        <p className="text-sm font-medium">Esquadria {i + 1}</p>
                        <BotaoRemover ariaLabel={`Remover esquadria ${i + 1}`} onClick={() => remove(i)}/>
                    </div>
                    <div className="grid gap-3 sm:grid-cols-2">
                        <Controller
                            control={control}
                            name={`esquadrias.${i}.tipo`}
                            render={({field, fieldState}) => (
                                <CampoEnum
                                    label="Tipo"
                                    opcoes={TipoEsquadria}
                                    value={String(field.value)}
                                    onChange={field.onChange}
                                    erro={fieldState.error?.message}
                                />
                            )}
                        />
                        <Controller
                            control={control}
                            name={`esquadrias.${i}.material`}
                            render={({field, fieldState}) => (
                                <CampoEnum
                                    label="Material"
                                    opcoes={MaterialEsquadria}
                                    value={String(field.value)}
                                    onChange={field.onChange}
                                    erro={fieldState.error?.message}
                                />
                            )}
                        />
                    </div>
                    <div className="grid gap-3 sm:grid-cols-3">
                        <CampoNumerico
                            label="Base (m)"
                            id={`esquadrias.${i}.geometria.base`}
                            step="0.01"
                            registration={register(`esquadrias.${i}.geometria.base`, {valueAsNumber: true})}
                            erro={errors.esquadrias?.[i]?.geometria?.base?.message}
                        />
                        <CampoNumerico
                            label="Altura (m)"
                            id={`esquadrias.${i}.geometria.altura`}
                            step="0.01"
                            registration={register(`esquadrias.${i}.geometria.altura`, {valueAsNumber: true})}
                            erro={errors.esquadrias?.[i]?.geometria?.altura?.message}
                        />
                        <CampoNumerico
                            label="Repetição"
                            id={`esquadrias.${i}.geometria.repeticao`}
                            inputMode="numeric"
                            registration={register(`esquadrias.${i}.geometria.repeticao`, {valueAsNumber: true})}
                            erro={errors.esquadrias?.[i]?.geometria?.repeticao?.message}
                        />
                    </div>
                    <div className="grid gap-3 sm:grid-cols-2">
                        <CampoNumerico
                            label="Peitoril (m)"
                            id={`esquadrias.${i}.alturaPeitoril`}
                            step="0.01"
                            registration={register(`esquadrias.${i}.alturaPeitoril`, {valueAsNumber: true})}
                            erro={errors.esquadrias?.[i]?.alturaPeitoril?.message}
                        />
                        <div className="space-y-1.5">
                            <Label htmlFor={`esquadrias.${i}.informacaoAdicional`}>Info adicional (opcional)</Label>
                            <Input
                                id={`esquadrias.${i}.informacaoAdicional`}
                                maxLength={INFO_ADICIONAL_MAX}
                                {...register(`esquadrias.${i}.informacaoAdicional`)}
                            />
                        </div>
                    </div>
                </div>
            ))}
            <Button
                type="button"
                variant="outline"
                onClick={() => append({...ESQUADRIA_VAZIA, geometria: {...ESQUADRIA_VAZIA.geometria}})}
            >
                <PlusIcon/> Adicionar esquadria
            </Button>
            {errors.esquadrias?.root && <ErroCampo mensagem={String(errors.esquadrias.root.message)}/>}
        </div>
    )
}
