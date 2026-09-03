import {Controller, useFieldArray, type UseFormReturn} from 'react-hook-form'
import {z} from 'zod'
import {PlusIcon} from 'lucide-react'
import {ModalFormulario} from '@/components/ambientes/ModalFormulario'
import {CampoEnum} from '@/components/ambientes/CampoEnum'
import {CampoNumerico} from '@/components/ambientes/CampoNumerico'
import {BotaoRemover} from '@/components/ambientes/BotaoRemover'
import {ErroCampo} from '@/components/ambientes/ErroCampo'
import {Button} from '@/components/ui/button'
import {geometriaSchema, type GeometriaInput} from '@/types/ambientes/request'
import {TipoGeometria} from '@/types/ambientes/enums'

const listaGeometriasSchema = z.object({
    geometrias: z.array(geometriaSchema).min(1, 'Pelo menos uma geometria'),
})
type ListaGeometrias = z.infer<typeof listaGeometriasSchema>

// Valores técnicos de enum (plano 11 §4) — base/altura 0 falham `positive()`
// apenas no submit, obrigando o preenchimento (mesmo padrão do FormAmbiente).
const GEOMETRIA_VAZIA: GeometriaInput = {tipo: 'RETANGULAR', base: 0, altura: 0, repeticao: 1}

interface ModalGeometriasProps {
    open: boolean
    modo: 'incluir' | 'editar'
    titulo: string
    // modo editar: pré-preenchimento já convertido para nomes técnicos
    // (lib/ambientes/mappers.geometriasDeDetalhe)
    inicial: GeometriaInput[]
    onOpenChange: (open: boolean) => void
    onSubmit: (geometrias: GeometriaInput[]) => Promise<void>
    onSalvou: () => void
}

// Genérico das UC08-FE (incluir) e UC09-FE (editar) — a página configura
// modo/titulo/inicial/onSubmit por UC (Alternativa A, sem wrappers).
export function ModalGeometrias({
                                    open,
                                    modo,
                                    titulo,
                                    inicial,
                                    onOpenChange,
                                    onSubmit,
                                    onSalvou,
                                }: ModalGeometriasProps) {
    return (
        <ModalFormulario
            open={open}
            title={titulo}
            schema={listaGeometriasSchema}
            defaults={{geometrias: inicial.length > 0 ? inicial : [GEOMETRIA_VAZIA]}}
            onOpenChange={onOpenChange}
            onSubmit={async ({geometrias}) => onSubmit(geometrias)}
            onSalvou={onSalvou}
            mensagemSucesso={modo === 'incluir' ? 'Geometrias incluídas.' : 'Geometrias atualizadas.'}
            mensagemPadrao={`Erro ao ${modo === 'incluir' ? 'incluir' : 'atualizar'} geometrias.`}
        >
            {(form) => <ListaGeometrias form={form}/>}
        </ModalFormulario>
    )
}

function ListaGeometrias({form}: { form: UseFormReturn<ListaGeometrias> }) {
    const {register, control, formState: {errors}} = form
    const {fields, append, remove} = useFieldArray({control, name: 'geometrias'})

    return (
        <div className="space-y-3">
            {fields.map((item, i) => (
                <div key={item.id} className="space-y-3 rounded-lg border p-4">
                    <div className="flex items-center justify-between">
                        <p className="text-sm font-medium">Geometria {i + 1}</p>
                        <BotaoRemover ariaLabel={`Remover geometria ${i + 1}`} onClick={() => remove(i)}/>
                    </div>
                    <Controller
                        control={control}
                        name={`geometrias.${i}.tipo`}
                        render={({field, fieldState}) => (
                            <CampoEnum
                                label="Tipo"
                                opcoes={TipoGeometria}
                                value={String(field.value)}
                                onChange={field.onChange}
                                erro={fieldState.error?.message}
                            />
                        )}
                    />
                    <div className="grid gap-3 sm:grid-cols-3">
                        <CampoNumerico
                            label="Base (m)"
                            id={`geometrias.${i}.base`}
                            step="0.01"
                            registration={register(`geometrias.${i}.base`, {valueAsNumber: true})}
                            erro={errors.geometrias?.[i]?.base?.message}
                        />
                        <CampoNumerico
                            label="Altura (m)"
                            id={`geometrias.${i}.altura`}
                            step="0.01"
                            registration={register(`geometrias.${i}.altura`, {valueAsNumber: true})}
                            erro={errors.geometrias?.[i]?.altura?.message}
                        />
                        <CampoNumerico
                            label="Repetição"
                            id={`geometrias.${i}.repeticao`}
                            inputMode="numeric"
                            registration={register(`geometrias.${i}.repeticao`, {valueAsNumber: true})}
                            erro={errors.geometrias?.[i]?.repeticao?.message}
                        />
                    </div>
                </div>
            ))}
            <Button type="button" variant="outline" onClick={() => append({...GEOMETRIA_VAZIA})}>
                <PlusIcon/> Adicionar geometria
            </Button>
            {errors.geometrias?.root && <ErroCampo mensagem={String(errors.geometrias.root.message)}/>}
        </div>
    )
}
