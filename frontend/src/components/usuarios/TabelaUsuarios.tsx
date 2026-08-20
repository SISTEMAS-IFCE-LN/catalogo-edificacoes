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

interface AcoesProps {
    usuario: User
    onEditarPerfis: (u: User) => void
    onAlterarStatus: (u: User, acao: StatusAcao) => void
}

function UsuarioActionsMenu({usuario, onEditarPerfis, onAlterarStatus}: AcoesProps) {
    return (
        <DropdownMenu>
            <DropdownMenuTrigger render={<Button variant="ghost" size="icon" aria-label={`Ações do usuário ${usuario.nome}`} />}>
                <MoreHorizontal className="h-4 w-4"/>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={() => onEditarPerfis(usuario)}>
                    Editar Perfis
                </DropdownMenuItem>
                {usuario.ativo ? (
                    <DropdownMenuItem onClick={() => onAlterarStatus(usuario, 'desativar')}>Desativar</DropdownMenuItem>
                ) : (
                    <DropdownMenuItem onClick={() => onAlterarStatus(usuario, 'ativar')}>Ativar</DropdownMenuItem>
                )}
            </DropdownMenuContent>
        </DropdownMenu>
    )
}

function StatusBadge({ativo}: { ativo: boolean }) {
    return (
        <Badge variant={ativo ? 'default' : 'outline'}>
            {ativo ? 'Ativo' : 'Inativo'}
        </Badge>
    )
}

export function TabelaUsuarios({itens, onEditarPerfis, onAlterarStatus}: Props) {
    return (
        <>
            {/* Cards empilhados em <md (§15.7) */}
            <div className="md:hidden space-y-3">
                {itens.map((u) => (
                    <div key={u.id} className="rounded-lg border bg-card p-3 shadow-sm">
                        <div className="flex items-start justify-between gap-2">
                            <div className="min-w-0">
                                <p className="font-semibold truncate">{u.nome}</p>
                                <p className="text-sm text-muted-foreground truncate">{u.email}</p>
                            </div>
                            <UsuarioActionsMenu usuario={u} onEditarPerfis={onEditarPerfis} onAlterarStatus={onAlterarStatus}/>
                        </div>
                        <div className="mt-2 flex flex-wrap gap-1">
                            <StatusBadge ativo={u.ativo}/>
                            {u.perfis.map((p) => (
                                <RoleBadge key={p} role={p}/>
                            ))}
                        </div>
                    </div>
                ))}
            </div>

            {/* Tabela completa em ≥md (§15.7) */}
            <div className="hidden md:block">
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
                                    <StatusBadge ativo={u.ativo}/>
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
                                    <UsuarioActionsMenu usuario={u} onEditarPerfis={onEditarPerfis} onAlterarStatus={onAlterarStatus}/>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </div>
        </>
    )
}
