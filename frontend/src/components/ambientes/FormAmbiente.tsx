import {useState} from 'react'
import {
    Controller,
    useFieldArray,
    useForm,
    useWatch,
    type Control,
    type Path,
    type UseFormReturn,
} from 'react-hook-form'
import {zodResolver} from '@hookform/resolvers/zod'
import {z} from 'zod'
import {PlusIcon} from 'lucide-react'
import {ambienteSchema, type AmbienteInput} from '@/types/ambientes/request'
import {
    Bloco,
    MaterialEsquadria,
    TipoAmbiente,
    TipoEsquadria,
    TipoGeometria,
    Unidade,
} from '@/types/ambientes/enums'
import {Button} from '@/components/ui/button'
import {Input} from '@/components/ui/input'
import {Label} from '@/components/ui/label'
import {Textarea} from '@/components/ui/textarea'
import {CampoEnum} from '@/components/ambientes/CampoEnum'
import {CampoNumerico} from '@/components/ambientes/CampoNumerico'
import {BotaoRemover} from '@/components/ambientes/BotaoRemover'
import {ErroCampo} from '@/components/ambientes/ErroCampo'

const ETAPAS = ['Dados Básicos', 'Geometrias', 'Pés-direitos', 'Esquadrias', 'Informação Adicional'] as const

// Máx. de caracteres da informação adicional (@Size(max=255) de AmbienteReq.kt)
const INFO_ADICIONAL_MAX = 255

// Validação por etapa (arquitetura §15.8): `trigger()` do schema COMPLETO falharia
// na etapa 0 (`geometrias.min(1)`, `pesDireitos.min(1)` e refine de porta ainda
// não preenchidos) e o wizard não avançaria.
const CAMPOS_POR_ETAPA: Array<Array<keyof AmbienteInput>> = [
    ['nome', 'localizacao', 'tipo', 'capacidade'],
    ['geometrias'],
    ['pesDireitos'],
    ['esquadrias'],
    ['informacaoAdicional'],
]

// Valores de `defaultValues`/`append` com NOMES TÉCNICOS de enum ('BLOCO_1',
// 'RETANGULAR', 'PORTA'...) — a resposta devolve rótulos, o request exige
// chaves (ver plano 11 §4 e arquitetura §13).
const GEOMETRIA_VAZIA = {tipo: 'RETANGULAR', base: 0, altura: 0, repeticao: 1} as const
const ESQUADRIA_VAZIA = {
    tipo: 'PORTA',
    geometria: {base: 0, altura: 0, repeticao: 1},
    material: 'NAO_SE_APLICA',
    alturaPeitoril: 0,
    informacaoAdicional: '',
} as const

type AmbienteFormValues = z.input<typeof ambienteSchema>

// Defaults do wizard (valores técnicos de enum — ver GEOMETRIA_VAZIA acima).
const DEFAULT_AMBIENTE_INPUT: AmbienteFormValues = {
    nome: '',
    localizacao: {bloco: 'BLOCO_1', unidade: 'SEDE', andar: 0},
    tipo: 'SALA_AULA',
    capacidade: 1,
    geometrias: [{...GEOMETRIA_VAZIA}],
    pesDireitos: [0],
    esquadrias: [{...ESQUADRIA_VAZIA, geometria: {...ESQUADRIA_VAZIA.geometria}}],
    informacaoAdicional: '',
}

interface Props {
    onSubmit: (data: AmbienteInput) => Promise<void>
    // Pré-preenchimento do wizard (UC16 ModalAlterarTipo): AmbienteReq completo
    // do ambiente atual, com nomes técnicos (lib/ambientes/mappers).
    initial?: AmbienteInput
}

export function FormAmbiente({onSubmit, initial}: Props) {
    const [etapa, setEtapa] = useState(0)
    const [submitting, setSubmitting] = useState(false)

    const form = useForm<AmbienteFormValues, unknown, AmbienteInput>({
        resolver: zodResolver(ambienteSchema),
        mode: 'onTouched',
        // Arrays com 1 item vazio para o useFieldArray renderizar o primeiro card.
        defaultValues: initial ?? DEFAULT_AMBIENTE_INPUT,
    })

    // Contador da 5ª etapa. O campo é opcional (`.optional().default('')` no
    // schema), então `trigger(['informacaoAdicional'])` passa vazio e o `max(255)`
    // só atua quando preenchido.
    const infoAdicional = useWatch({control: form.control, name: 'informacaoAdicional'}) ?? ''

    async function proximo() {
        const ok = await form.trigger(CAMPOS_POR_ETAPA[etapa])
        if (ok) setEtapa((e) => Math.min(e + 1, ETAPAS.length - 1))
    }

    function voltar() {
        setEtapa((e) => Math.max(e - 1, 0))
    }

    async function salvar(values: AmbienteInput) {
        setSubmitting(true)
        try {
            await onSubmit(values)
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <form onSubmit={form.handleSubmit(salvar)} className="space-y-6">
            <div className="text-sm text-muted-foreground">
                Etapa {etapa + 1} de {ETAPAS.length}: <strong>{ETAPAS[etapa]}</strong>
            </div>

            {etapa === 0 && <EtapaDadosBasicos form={form}/>}
            {etapa === 1 && <EtapaGeometrias form={form}/>}
            {etapa === 2 && <EtapaPesDireitos form={form}/>}
            {etapa === 3 && <EtapaEsquadrias form={form}/>}
            {etapa === 4 && (
                <div className="space-y-3">
                    <Label htmlFor="informacaoAdicional">Informação Adicional (opcional)</Label>
                    <Textarea
                        id="informacaoAdicional"
                        rows={4}
                        maxLength={INFO_ADICIONAL_MAX}
                        placeholder="Ex.: sala com ar-condicionado"
                        {...form.register('informacaoAdicional')}
                    />
                    <p className="text-right text-xs text-muted-foreground">
                        {infoAdicional.length}/{INFO_ADICIONAL_MAX}
                    </p>
                    {form.formState.errors.informacaoAdicional && (
                        <ErroCampo mensagem={String(form.formState.errors.informacaoAdicional.message)}/>
                    )}
                </div>
            )}

            <div className="flex flex-wrap gap-2">
                {etapa > 0 && (
                    <Button type="button" variant="outline" onClick={voltar}>
                        Voltar
                    </Button>
                )}
                {etapa < ETAPAS.length - 1 && (
                    <Button type="button" onClick={proximo}>
                        Próximo
                    </Button>
                )}
                {etapa === ETAPAS.length - 1 && (
                    <Button type="submit" disabled={submitting}>
                        {submitting ? 'Salvando…' : 'Salvar'}
                    </Button>
                )}
            </div>
        </form>
    )
}

type FormProp = { form: UseFormReturn<AmbienteFormValues, unknown, AmbienteInput> }

function EtapaDadosBasicos({form}: FormProp) {
    const {
        register,
        control,
        formState: {errors},
    } = form
    return (
        <div className="space-y-3">
            <div className="space-y-1.5">
                <Label htmlFor="nome">Nome</Label>
                <Input id="nome" maxLength={50} {...register('nome')} />
                {errors.nome && <ErroCampo mensagem={String(errors.nome.message)}/>}
            </div>
            <CampoEnumDeForm label="Bloco" control={control} name="localizacao.bloco" opcoes={Bloco}/>
            <CampoEnumDeForm label="Unidade" control={control} name="localizacao.unidade" opcoes={Unidade}/>
            <CampoNumerico
                label="Andar"
                id="localizacao.andar"
                inputMode="numeric"
                registration={register('localizacao.andar', {valueAsNumber: true})}
                erro={errors.localizacao?.andar?.message}
            />
            <CampoEnumDeForm label="Tipo" control={control} name="tipo" opcoes={TipoAmbiente}/>
            <CampoNumerico
                label="Capacidade"
                id="capacidade"
                inputMode="numeric"
                registration={register('capacidade', {valueAsNumber: true})}
                erro={errors.capacidade?.message}
            />
        </div>
    )
}

function EtapaGeometrias({form}: FormProp) {
    const {
        register,
        control,
        formState: {errors},
    } = form
    const {fields, append, remove} = useFieldArray({control, name: 'geometrias'})
    return (
        <div className="space-y-3">
            {fields.map((item, i) => (
                <div key={item.id} className="space-y-3 rounded-lg border p-4">
                    <div className="flex items-center justify-between">
                        <p className="text-sm font-medium">Geometria {i + 1}</p>
                        <BotaoRemover ariaLabel={`Remover geometria ${i + 1}`} onClick={() => remove(i)}/>
                    </div>
                    <CampoEnumDeForm label="Tipo" control={control} name={`geometrias.${i}.tipo`} opcoes={TipoGeometria}/>
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

function EtapaPesDireitos({form}: FormProp) {
    const {
        register,
        control,
        formState: {errors},
        setValue,
    } = form
    // `useFieldArray` exige arrays de objetos; `pesDireitos` é `number[]`
    // (contrato do backend), então a lista é gerenciada com useWatch/setValue.
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

function EtapaEsquadrias({form}: FormProp) {
    const {
        register,
        control,
        formState: {errors},
    } = form
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
                        <CampoEnumDeForm label="Tipo" control={control} name={`esquadrias.${i}.tipo`} opcoes={TipoEsquadria}/>
                        <CampoEnumDeForm label="Material" control={control} name={`esquadrias.${i}.material`}
                                         opcoes={MaterialEsquadria}/>
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

// Adapta o CampoEnum compartilhado (value/onChange) ao Controller do RHF —
// mantém a assinatura (control + name) usada nas etapas do wizard.
function CampoEnumDeForm({
                             label,
                             control,
                             name,
                             opcoes,
                         }: {
    label: string
    control: Control<AmbienteFormValues>
    name: Path<AmbienteFormValues>
    opcoes: Record<string, string>
}) {
    return (
        <Controller
            control={control}
            name={name}
            render={({field, fieldState}) => (
                <CampoEnum
                    label={label}
                    opcoes={opcoes}
                    value={String(field.value)}
                    onChange={field.onChange}
                    erro={fieldState.error?.message}
                />
            )}
        />
    )
}
