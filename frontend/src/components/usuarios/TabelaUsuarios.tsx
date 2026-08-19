import {
    Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import {Badge} from '@/components/ui/badge'
import {Button} from '@/components/ui/button'
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from '@/components/ui/dropdown-menu'
import {MoreHorizontal} from 'lucide-react'
import type {User, StatusAcao} from '@/types/usuarios/user'
import {RoleBadge} from '@/components/usuarios/RoleBadge'

interface Props {
    itens: User[]
    onEditarPerfis: (u: User) => void
    onAlterarStatus: (u: User, acao: StatusAcao) => void
}

export function TabelaUsuarios({itens, onEditarPerfis, onAlterarStatus}: Props) {
    return (
        <Table>
            <TableHeader>
                <TableRow>
                    <TableHead>ID</TableHead>
                    <TableHead>Email</TableHead>
                    <TableHead>Nome</TableHead>
                    <TableHead>Ativo</TableHead>
                    <TableHead>Criado em</TableHead>
                    <TableHead>Perfis</TableHead>
                    <TableHead className="w-10"/>
                </TableRow>
            </TableHeader>
            <TableBody>
                {itens.map((u) => (
                    <TableRow key={u.id}>
                        <TableCell>{u.id}</TableCell>
                        <TableCell>{u.email}</TableCell>
                        <TableCell>{u.nome}</TableCell>
                        <TableCell>
                            <Badge variant={u.ativo ? 'default' : 'outline'}>
                                {u.ativo ? 'Ativo' : 'Inativo'}
                            </Badge>
                        </TableCell>
                        <TableCell>{new Date(u.criadoEm).toLocaleDateString('pt-BR')}</TableCell>
                        <TableCell>
                            <div className="flex flex-wrap gap-1">
                                {u.perfis.map((p) => (
                                    <RoleBadge key={p} role={p}/>
                                ))}
                            </div>
                        </TableCell>
                        <TableCell>
                            <DropdownMenu>
                                <DropdownMenuTrigger render={<Button variant="ghost" size="icon" />}>
                                    <MoreHorizontal className="h-4 w-4"/>
                                </DropdownMenuTrigger>
                                <DropdownMenuContent align="end">
                                    <DropdownMenuItem onClick={() => onEditarPerfis(u)}>
                                        Editar Perfis
                                    </DropdownMenuItem>
                                    {u.ativo ? (
                                        <DropdownMenuItem onClick={() => onAlterarStatus(u, 'desativar')}>Desativar</DropdownMenuItem>
                                    ) : (
                                        <DropdownMenuItem onClick={() => onAlterarStatus(u, 'ativar')}>Ativar</DropdownMenuItem>
                                    )}
                                </DropdownMenuContent>
                            </DropdownMenu>
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    )
}