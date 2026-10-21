import {useState} from 'react'
import {Label} from '@/components/ui/label'
import {Input} from '@/components/ui/input'
import {Button} from '@/components/ui/button'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select'
import {ErroCampo} from '@/components/ambientes/ErroCampo'
import {
    REFERENCIA_VAZIA,
    type ReferenciaInput,
    type ReferenciaRes,
} from '@/types/ambientes/referencia'

const VALOR_CADASTRAR = '__cadastrar__'

interface CampoReferenciaProps {
    label: string
    value: ReferenciaInput
    onChange: (valor: ReferenciaInput) => void
    opcoes: ReferenciaRes[]
    carregando?: boolean
    erro?: string
    permitirCadastro?: boolean   // default true
    somenteCadastro?: boolean    // pai novo: não há opções existentes
    disabled?: boolean
    placeholder?: string
}

// Campo genérico de referência (campus/unidade/bloco): lista os existentes e,
// opcionalmente, troca para o input de cadastro por nome. O pai decide se o
// cadastro é a única opção (`somenteCadastro`, quando ele próprio é novo).
export function CampoReferencia({
    label,
    value,
    onChange,
    opcoes,
    carregando = false,
    erro,
    permitirCadastro = true,
    somenteCadastro = false,
    disabled = false,
    placeholder = 'Selecione...',
}: CampoReferenciaProps) {
    const [modoCadastro, setModoCadastro] = useState(
        somenteCadastro || (value.id === null && value.nome !== null)
    )

    function selecionar(valor: string) {
        if (valor === VALOR_CADASTRAR) {
            setModoCadastro(true)
            onChange({id: null, nome: ''})
            return
        }
        onChange({id: Number(valor), nome: null})
    }

    function voltar() {
        setModoCadastro(false)
        onChange(REFERENCIA_VAZIA)
    }

    return (
        <div className="space-y-1.5">
            <Label>{label}</Label>
            {(modoCadastro || somenteCadastro) ? (
                <div className="flex gap-2">
                    <Input
                        aria-label={`${label} (novo)`}
                        maxLength={50}
                        value={value.nome ?? ''}
                        onChange={(e) => onChange({id: null, nome: e.target.value})}
                        placeholder={`Digite o nome de ${label.toLowerCase()}`}
                        disabled={disabled}
                    />
                    {!somenteCadastro && (
                        <Button type="button" variant="outline" onClick={voltar}>
                            Voltar
                        </Button>
                    )}
                </div>
            ) : (
                <Select
                    value={value.id !== null ? String(value.id) : ''}
                    onValueChange={(v) => {
                        if (v !== null) selecionar(v)
                    }}
                    disabled={disabled || carregando}
                >
                    <SelectTrigger aria-label={label} className="w-full">
                        {value.id !== null
                            ? <span>{opcoes.find((o) => o.id === value.id)?.nome ?? placeholder}</span>
                            : <SelectValue placeholder={carregando ? 'Carregando...' : placeholder}/>}
                    </SelectTrigger>
                    <SelectContent>
                        {opcoes.map((o) => (
                            <SelectItem key={o.id} value={String(o.id)}>{o.nome}</SelectItem>
                        ))}
                        {permitirCadastro && (
                            <SelectItem value={VALOR_CADASTRAR}>+ Cadastrar novo</SelectItem>
                        )}
                    </SelectContent>
                </Select>
            )}
            {erro && <ErroCampo mensagem={erro}/>}
        </div>
    )
}
