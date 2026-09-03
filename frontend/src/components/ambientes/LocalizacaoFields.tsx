import {Controller, type Control, type FieldErrors, type UseFormRegister, type UseFormReturn} from 'react-hook-form'
import {CampoEnum} from '@/components/ambientes/CampoEnum'
import {CampoNumerico} from '@/components/ambientes/CampoNumerico'
import {Bloco, Unidade} from '@/types/ambientes/enums'
import type {LocalizacaoInput} from '@/types/ambientes/request'

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

// Grupo compartilhado de localização: Bloco/Unidade (value = chave técnica,
// texto = rótulo — plano 11 §4) e Andar (numérico, @Min(0)).
export function LocalizacaoFields<T extends ComLocalizacao>({form, idPrefix = 'loc'}: LocalizacaoFieldsProps<T>) {
    // Os campos compartilhados operam só sobre o grupo `localizacao`; o cast
    // restringe o form genérico à parte que este bloco conhece.
    type FormLocal = ComLocalizacao
    const register = form.register as unknown as UseFormRegister<FormLocal>
    const control = form.control as unknown as Control<FormLocal>
    const erros = form.formState.errors as unknown as FieldErrors<FormLocal>

    return (
        <>
            <Controller
                control={control}
                name="localizacao.bloco"
                render={({field, fieldState}) => (
                    <CampoEnum
                        label="Bloco"
                        opcoes={Bloco}
                        value={String(field.value)}
                        onChange={field.onChange}
                        erro={fieldState.error?.message}
                    />
                )}
            />
            <Controller
                control={control}
                name="localizacao.unidade"
                render={({field, fieldState}) => (
                    <CampoEnum
                        label="Unidade"
                        opcoes={Unidade}
                        value={String(field.value)}
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
