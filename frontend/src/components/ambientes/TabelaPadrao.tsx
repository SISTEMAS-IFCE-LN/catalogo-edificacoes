import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table'
import { Checkbox } from '@/components/ui/checkbox'
import type { AmbienteBasico } from '@/types/ambientes/ambiente'
import { Link } from 'react-router'

interface TabelaPadraoProps {
    itens: AmbienteBasico[]
    /** Base da rota de detalhe. Default: '/ambientes/publicados' (lista pública). */
    detalheBasePath?: string
    /** Quando ausente, a tabela não exibe checkboxes (modo público/anônimo). */
    selectedIds?: number[]
    onToggleSelect?: (id: number) => void
    onToggleSelectAll?: () => void
    allSelected?: boolean
    someSelected?: boolean
}

function formatarAndar(andar: number): string {
    return andar === 0 ? 'Térreo' : `${andar}º`
}

export function TabelaPadrao({
    itens,
    detalheBasePath = '/ambientes/publicados',
    selectedIds,
    onToggleSelect,
    onToggleSelectAll,
    allSelected,
    someSelected,
}: TabelaPadraoProps) {
    const comSelecao = typeof onToggleSelect === 'function'

    return (
        <Table>
            <TableHeader>
                <TableRow>
                    {comSelecao && (
                        <TableHead className="w-[40px]">
                            <Checkbox
                                aria-label="Selecionar todos da página"
                                checked={allSelected ?? false}
                                indeterminate={someSelected && !allSelected}
                                onCheckedChange={() => onToggleSelectAll?.()}
                            />
                        </TableHead>
                    )}
                    <TableHead>Nome</TableHead>
                    <TableHead>Bloco</TableHead>
                    <TableHead>Unidade</TableHead>
                    <TableHead>Andar</TableHead>
                    <TableHead>Tipo</TableHead>
                    <TableHead>Capacidade</TableHead>
                    <TableHead>Área</TableHead>
                </TableRow>
            </TableHeader>
            <TableBody>
                {itens.map((a) => {
                    const selecionado = comSelecao && selectedIds?.includes(a.id)

                    return (
                        <TableRow key={a.id} data-selected={selecionado || undefined}>
                            {comSelecao && (
                                <TableCell className="w-[40px]">
                                    <Checkbox
                                        aria-label={`Selecionar ${a.nome}`}
                                        checked={selecionado ?? false}
                                        onCheckedChange={() => onToggleSelect?.(a.id)}
                                    />
                                </TableCell>
                            )}
                            <TableCell>
                                <Link to={`${detalheBasePath}/${a.id}`} className="text-primary hover:underline">
                                    {a.nome}
                                </Link>
                            </TableCell>
                            <TableCell>{a.localizacao.bloco}</TableCell>
                            <TableCell>{a.localizacao.unidade}</TableCell>
                            <TableCell>{formatarAndar(a.localizacao.andar)}</TableCell>
                            <TableCell>{a.tipo}</TableCell>
                            <TableCell>{a.capacidade}</TableCell>
                            <TableCell>{a.area.toFixed(2)}</TableCell>
                        </TableRow>
                    )
                })}
            </TableBody>
        </Table>
    )
}