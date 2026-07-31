import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table'
import type {AmbienteBasico} from '@/types/ambiente'
import {Link} from 'react-router'

export function TabelaPadrao({itens}: { itens: AmbienteBasico[] }) {
    return (
        <Table>
            <TableHeader>
                <TableRow>
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
                {itens.map((a) => (
                    <TableRow key={a.id}>
                        <TableCell>
                            <Link to={`/ambientes/publicados/${a.id}`} className="text-primary hover:underline">
                                {a.nome}
                            </Link>
                        </TableCell>
                        <TableCell>{a.localizacao.bloco}</TableCell>
                        <TableCell>{a.localizacao.unidade}</TableCell>
                        <TableCell>{a.localizacao.andar}</TableCell>
                        <TableCell>{a.tipo}</TableCell>
                        <TableCell>{a.capacidade}</TableCell>
                        <TableCell>{a.area.toFixed(2)}</TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    )
}