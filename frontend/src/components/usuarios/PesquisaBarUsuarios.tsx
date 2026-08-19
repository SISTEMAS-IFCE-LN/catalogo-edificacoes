import {useState} from 'react'
import {Input} from '@/components/ui/input'
import {Button} from '@/components/ui/button'
import {
    Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import {useFiltroLocal} from '@/hooks/useFiltroLocal'
import {TipoFiltroUsuarios, FILTROS_USUARIOS_VAZIOS, type FiltrosUsuarios} from '@/types/usuarios/filtros'

interface Props {
    /** Filtro vigente (vindo da URL). */
    initial: FiltrosUsuarios
    onChange: (f: FiltrosUsuarios) => void
}

/**
 * Barra de busca da página /usuarios: um select escolhe o tipo de filtro
 * (nome ou email) e um input dinâmico coleta o termo. Busca aplicada via botão
 * "Buscar" (sem debounce). Espelha `PesquisaBarAmbientes`.
 */
export function PesquisaBarUsuarios({initial, onChange}: Props) {
    // Detectar tipo de filtro inicial baseado nos valores iniciais
    const [tipoFiltro, setTipoFiltro] = useState<TipoFiltroUsuarios>(() => {
        if (initial.nome) return TipoFiltroUsuarios.NOME
        if (initial.email) return TipoFiltroUsuarios.EMAIL
        return TipoFiltroUsuarios.NENHUM
    })

    const {local, setLocal} = useFiltroLocal<FiltrosUsuarios>(initial, () => {
        // Re-derivar tipo de filtro quando initial muda externamente (back/forward)
        if (initial.nome) setTipoFiltro(TipoFiltroUsuarios.NOME)
        else if (initial.email) setTipoFiltro(TipoFiltroUsuarios.EMAIL)
        else setTipoFiltro(TipoFiltroUsuarios.NENHUM)
    })

    function handleTipoFiltroChange(value: string | null) {
        setTipoFiltro((value ?? '') as TipoFiltroUsuarios)
        // Limpar filtros ao trocar de tipo
        setLocal(FILTROS_USUARIOS_VAZIOS)
    }

    function handleAplicar() {
        if (tipoFiltro === TipoFiltroUsuarios.NOME && !local.nome.trim()) return
        if (tipoFiltro === TipoFiltroUsuarios.EMAIL && !local.email.trim()) return
        onChange(local)
    }

    function handleLimpar() {
        setLocal(FILTROS_USUARIOS_VAZIOS)
        setTipoFiltro(TipoFiltroUsuarios.NENHUM)
        onChange(FILTROS_USUARIOS_VAZIOS)
    }

    const temFiltros = initial.nome !== '' || initial.email !== ''

    return (
        <div className="space-y-4">
            {/* Seletor de tipo de filtro */}
            <div className="max-w-xs">
                <Select value={tipoFiltro} onValueChange={handleTipoFiltroChange}>
                    <SelectTrigger aria-label="Tipo de filtro">
                        <SelectValue placeholder="Selecione o tipo de filtro"/>
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value={TipoFiltroUsuarios.NENHUM}>Nenhum</SelectItem>
                        <SelectItem value={TipoFiltroUsuarios.NOME}>Nome</SelectItem>
                        <SelectItem value={TipoFiltroUsuarios.EMAIL}>Email</SelectItem>
                    </SelectContent>
                </Select>
            </div>

            {tipoFiltro === TipoFiltroUsuarios.NOME && (
                <div className="max-w-md">
                    <Input
                        placeholder="Buscar por nome…"
                        aria-label="Buscar usuário por nome"
                        value={local.nome}
                        onChange={(e) => setLocal({...local, nome: e.target.value})}
                        maxLength={100}
                    />
                </div>
            )}

            {tipoFiltro === TipoFiltroUsuarios.EMAIL && (
                <div className="max-w-md">
                    <Input
                        type="email"
                        placeholder="Buscar por email…"
                        aria-label="Buscar usuário por email"
                        value={local.email}
                        onChange={(e) => setLocal({...local, email: e.target.value})}
                        maxLength={255}
                    />
                </div>
            )}

            {/* Botões */}
            <div className="flex gap-2">
                <Button variant="outline" onClick={handleAplicar} disabled={tipoFiltro === TipoFiltroUsuarios.NENHUM}>
                    Buscar
                </Button>
                {temFiltros && (
                    <Button variant="ghost" onClick={handleLimpar}>
                        Limpar
                    </Button>
                )}
            </div>
        </div>
    )
}
