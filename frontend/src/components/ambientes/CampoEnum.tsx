import {Label} from '@/components/ui/label'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select'
import {ErroCampo} from '@/components/ambientes/ErroCampo'

interface CampoEnumProps {
    label: string
    opcoes: Record<string, string>
    value: string
    onChange: (valor: string) => void
    erro?: string
}

// Select de enum compartilhado: value = chave técnica ('BLOCO_1'), texto =
// rótulo ('Bloco 1') — o backend desserializa enums pelo NOME (plano 11 §4).
// Apresentacional: a ligação com o RHF fica no chamador (Controller/register).
export function CampoEnum({label, opcoes, value, onChange, erro}: CampoEnumProps) {
    return (
        <div className="space-y-1.5">
            <Label>{label}</Label>
            <Select value={value} onValueChange={(v) => {
                if (v !== null) onChange(v)
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
            {erro && <ErroCampo mensagem={erro}/>}
        </div>
    )
}
