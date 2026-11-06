import {
    Controller,
    useWatch,
    type Control,
    type FieldErrors,
    type UseFormRegister,
    type UseFormSetValue,
    type UseFormReturn,
} from 'react-hook-form'
import {CampoReferencia} from '@/components/ambientes/CampoReferencia'
import {CampoNumerico} from '@/components/ambientes/CampoNumerico'
import {REFERENCIA_VAZIA} from '@/types/ambientes/referencia'
import type {LocalizacaoInput} from '@/types/ambientes/request'
import {useBlocos, useCampus, useUnidades} from '@/hooks/useReferencias'

// Qualquer form que tenha o grupo `localizacao` (DadosBasicosInput UC07,
// DuplicacaoInput UC17, AmbienteInput UC06/UC16).
interface ComLocalizacao {
    localizacao: LocalizacaoInput
}

interface LocalizacaoFieldsProps<T extends ComLocalizacao> {
    form: UseFormReturn<T>
    // Prefixo dos ids dos inputs — evita ids duplicados na página.
    idPrefix?: string
}

// Grupo compartilhado: Campus → Unidade → Bloco → Andar.
// O filho é resetado quando o pai muda; se o pai é novo (id null), o filho
// só pode ser criado (somenteCadastro).
export function LocalizacaoFields<T extends ComLocalizacao>({form, idPrefix = 'loc'}: LocalizacaoFieldsProps<T>) {
    // Os campos compartilhados operam só sobre o grupo `localizacao`; o cast
    // restringe o form genérico à parte que este bloco conhece.
    type FormLocal = ComLocalizacao
    const register = form.register as unknown as UseFormRegister<FormLocal>
    const control = form.control as unknown as Control<FormLocal>
    const erros = form.formState.errors as unknown as FieldErrors<FormLocal>
    const setValue = form.setValue as unknown as UseFormSetValue<FormLocal>

    const campus = useWatch({control, name: 'localizacao.campus'}) ?? REFERENCIA_VAZIA
    const unidade = useWatch({control, name: 'localizacao.unidade'}) ?? REFERENCIA_VAZIA

    const {data: campi = [], isLoading: carregandoCampi} = useCampus()
    const {data: unidades = [], isLoading: carregandoUnidades} = useUnidades(campus.id)
    const {data: blocos = [], isLoading: carregandoBlocos} = useBlocos(unidade.id)

    return (
        <>
            <Controller
                control={control}
                name="localizacao.campus"
                render={({field, fieldState}) => (
                    <CampoReferencia
                        label="Campus"
                        value={field.value}
                        opcoes={campi}
                        carregando={carregandoCampi}
                        onChange={(v) => {
                            field.onChange(v)
                            setValue('localizacao.unidade', {...REFERENCIA_VAZIA}, {shouldDirty: true})
                            setValue('localizacao.bloco', {...REFERENCIA_VAZIA}, {shouldDirty: true})
                        }}
                        erro={fieldState.error?.message}
                    />
                )}
            />
            <Controller
                control={control}
                name="localizacao.unidade"
                render={({field, fieldState}) => (
                    <CampoReferencia
                        key={campus.id ?? campus.nome ?? 'vazio'}
                        label="Unidade"
                        value={field.value}
                        opcoes={unidades}
                        carregando={carregandoUnidades}
                        disabled={campus.id === null && campus.nome === null}
                        somenteCadastro={campus.id === null && campus.nome !== null}
                        onChange={(v) => {
                            field.onChange(v)
                            setValue('localizacao.bloco', {...REFERENCIA_VAZIA}, {shouldDirty: true})
                        }}
                        erro={fieldState.error?.message}
                    />
                )}
            />
            <Controller
                control={control}
                name="localizacao.bloco"
                render={({field, fieldState}) => (
                    <CampoReferencia
                        key={unidade.id ?? unidade.nome ?? 'vazio'}
                        label="Bloco"
                        value={field.value}
                        opcoes={blocos}
                        carregando={carregandoBlocos}
                        disabled={unidade.id === null && unidade.nome === null}
                        somenteCadastro={unidade.id === null && unidade.nome !== null}
                        onChange={field.onChange}
                        erro={fieldState.error?.message}
                    />
                )}
            />
            <CampoNumerico
                label="Andar"
                id={`${idPrefix}-andar`}
                inputMode="numeric"
                registration={register('localizacao.andar', {valueAsNumber: true})}
                erro={erros.localizacao?.andar?.message}
            />
        </>
    )
}
