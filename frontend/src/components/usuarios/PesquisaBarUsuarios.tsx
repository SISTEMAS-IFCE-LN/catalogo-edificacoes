import {Input} from '@/components/ui/input'
import {Button} from '@/components/ui/button'
import {useFiltroLocal} from '@/hooks/useFiltroLocal'

interface Props {
    initial: string
    onChange: (nome: string) => void
    temFiltro: boolean
}

export function PesquisaBarUsuarios({initial, onChange, temFiltro}: Props) {
    const {local, setLocal} = useFiltroLocal(initial)

    function handleAplicar() {
        const nome = local.trim()
        if (!nome) return
        onChange(nome)
    }

    function handleLimpar() {
        setLocal('')
        onChange('')
    }

    return (
        <div className="flex gap-2">
            <Input
                placeholder="Buscar por nome…"
                aria-label="Buscar usuário por nome"
                value={local}
                onChange={(e) => setLocal(e.target.value)}
                maxLength={100}
                className="max-w-sm"
                onKeyDown={(e) => {
                    if (e.key === 'Enter') handleAplicar()
                }}
            />
            <Button variant="outline" onClick={handleAplicar} disabled={!local.trim()}>
                Buscar
            </Button>
            {temFiltro && (
                <Button variant="ghost" onClick={handleLimpar}>
                    Limpar
                </Button>
            )}
        </div>
    )
}