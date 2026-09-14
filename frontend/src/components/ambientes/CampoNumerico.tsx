import type {UseFormRegisterReturn} from 'react-hook-form'
import {Input} from '@/components/ui/input'
import {Label} from '@/components/ui/label'
import {ErroCampo} from '@/components/ambientes/ErroCampo'

interface CampoNumericoProps {
    label: string
    id: string
    // Presentacional: o chamador produz o registration com register(caminho,
    // {valueAsNumber: true}) — evita generics de RHF no componente compartilhado.
    registration: UseFormRegisterReturn
    erro?: string
    // Step nativo do input numérico: '0.01' para medidas decimais (espelha
    // @Digits(integer=7, fraction=2)); sem step (inteiro) em contagens. Sem o
    // step, o browser bloqueia o submit por stepMismatch em valores decimais
    // (constraint validation nativa) e os erros inline do Zod não aparecem.
    step?: string
    inputMode?: 'decimal' | 'numeric'
}

export function CampoNumerico({
                                   label,
                                   id,
                                   registration,
                                   erro,
                                   step,
                                   inputMode = 'decimal',
                               }: CampoNumericoProps) {
    return (
        <div className="space-y-1.5">
            <Label htmlFor={id}>{label}</Label>
            <Input
                id={id}
                type="number"
                inputMode={inputMode}
                step={step}
                {...registration}
            />
            {erro && <ErroCampo mensagem={erro}/>}
        </div>
    )
}
