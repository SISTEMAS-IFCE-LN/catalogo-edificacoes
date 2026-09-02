import {useState} from 'react'
import {
    Controller,
    useFieldArray,
    useForm,
    useWatch,
    type Control,
    type Path,
    type UseFormRegister,
    type UseFormReturn,
} from 'react-hook-form'
import {zodResolver} from '@hookform/resolvers/zod'
import {z} from 'zod'
import {PlusIcon, Trash2Icon} from 'lucide-react'
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
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select'

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

interface Props {
    onSubmit: (data: AmbienteInput) => Promise<void>
}

export function FormAmbiente({onSubmit}: Props) {
    const [etapa, setEtapa] = useState(0)
    const [submitting, setSubmitting] = useState(false)

    const form = useForm<AmbienteFormValues, unknown, AmbienteInput>({
        resolver: zodResolver(ambienteSchema),
        mode: 'onTouched',
        // Arrays com 1 item vazio para o useFieldArray renderizar o primeiro card.
        defaultValues: {
            nome: '',
            localizacao: {bloco: 'BLOCO_1', unidade: 'SEDE', andar: 0},
            tipo: 'SALA_AULA',
            capacidade: 1,
            geometrias: [{...GEOMETRIA_VAZIA}],
            pesDireitos: [0],
            esquadrias: [{...ESQUADRIA_VAZIA, geometria: {...ESQUADRIA_VAZIA.geometria}}],
            informacaoAdicional: '',
        },
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
            <CampoEnum label="Bloco" control={control} name="localizacao.bloco" opcoes={Bloco}/>
            <CampoEnum label="Unidade" control={control} name="localizacao.unidade" opcoes={Unidade}/>
            <CampoNumero
                label="Andar"
                register={register}
                name="localizacao.andar"
                inputMode="numeric"
                min={0}
                erro={errors.localizacao?.andar?.message}
            />
            <CampoEnum label="Tipo" control={control} name="tipo" opcoes={TipoAmbiente}/>
            <CampoNumero
                label="Capacidade"
                register={register}
                name="capacidade"
                inputMode="numeric"
                min={1}
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
                    <CampoEnum label="Tipo" control={control} name={`geometrias.${i}.tipo`} opcoes={TipoGeometria}/>
                    <div className="grid gap-3 sm:grid-cols-3">
                        <CampoNumero
                            label="Base (m)"
                            register={register}
                            name={`geometrias.${i}.base`}
                            erro={errors.geometrias?.[i]?.base?.message}
                        />
                        <CampoNumero
                            label="Altura (m)"
                            register={register}
                            name={`geometrias.${i}.altura`}
                            erro={errors.geometrias?.[i]?.altura?.message}
                        />
                        <CampoNumero
                            label="Repetição"
                            register={register}
                            name={`geometrias.${i}.repeticao`}
                            inputMode="numeric"
                            min={1}
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
                        <CampoNumero
                            label={`Pé-direito ${i + 1} (m)`}
                            register={register}
                            name={`pesDireitos.${i}`}
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
                        <CampoEnum label="Tipo" control={control} name={`esquadrias.${i}.tipo`} opcoes={TipoEsquadria}/>
                        <CampoEnum label="Material" control={control} name={`esquadrias.${i}.material`}
                                   opcoes={MaterialEsquadria}/>
                    </div>
                    <div className="grid gap-3 sm:grid-cols-3">
                        <CampoNumero
                            label="Base (m)"
                            register={register}
                            name={`esquadrias.${i}.geometria.base`}
                            erro={errors.esquadrias?.[i]?.geometria?.base?.message}
                        />
                        <CampoNumero
                            label="Altura (m)"
                            register={register}
                            name={`esquadrias.${i}.geometria.altura`}
                            erro={errors.esquadrias?.[i]?.geometria?.altura?.message}
                        />
                        <CampoNumero
                            label="Repetição"
                            register={register}
                            name={`esquadrias.${i}.geometria.repeticao`}
                            inputMode="numeric"
                            min={1}
                            erro={errors.esquadrias?.[i]?.geometria?.repeticao?.message}
                        />
                    </div>
                    <div className="grid gap-3 sm:grid-cols-2">
                        <CampoNumero
                            label="Peitoril (m)"
                            register={register}
                            name={`esquadrias.${i}.alturaPeitoril`}
                            min={0}
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

// Opções vindas dos enums TS: value = chave técnica ('BLOCO_1'), texto = rótulo
// ('Bloco 1') — ver plano 11 §4.
function CampoEnum({
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
        <div className="space-y-1.5">
            <Label>{label}</Label>
            <Controller
                control={control}
                name={name}
                render={({field, fieldState}) => (
                    <>
                        <Select value={field.value as string} onValueChange={(v) => {
                            if (v !== null) field.onChange(v)
                        }}>
                            <SelectTrigger aria-label={label} className="w-full">
                                <SelectValue/>
                            </SelectTrigger>
                            <SelectContent>
                                {Object.entries(opcoes).map(([chave, rotulo]) => (
                                    <SelectItem key={chave} value={chave}>
                                        {rotulo}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                        {fieldState.error && <ErroCampo mensagem={String(fieldState.error.message)}/>}
                    </>
                )}
            />
        </div>
    )
}

function CampoNumero({
                         label,
                         register,
                         name,
                         erro,
                         min,
                         inputMode = 'decimal',
                     }: {
    label: string
    register: UseFormRegister<AmbienteFormValues>
    name: Path<AmbienteFormValues>
    erro?: string
    min?: number
    inputMode?: 'decimal' | 'numeric'
}) {
    return (
        <div className="space-y-1.5">
            <Label htmlFor={name}>{label}</Label>
            <Input
                id={name}
                type="number"
                inputMode={inputMode}
                min={min}
                {...register(name, {valueAsNumber: true})}
            />
            {erro && <ErroCampo mensagem={erro}/>}
        </div>
    )
}

function BotaoRemover({ariaLabel, onClick}: { ariaLabel: string; onClick: () => void }) {
    return (
        <Button type="button" variant="ghost" size="icon" aria-label={ariaLabel} onClick={onClick}>
            <Trash2Icon/>
        </Button>
    )
}

function ErroCampo({mensagem}: { mensagem: string }) {
    return (
        <p role="alert" className="text-sm text-destructive">
            {mensagem}
        </p>
    )
}
