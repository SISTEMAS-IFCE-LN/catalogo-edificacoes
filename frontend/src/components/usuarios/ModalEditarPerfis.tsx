import {useState} from 'react'
import {
    Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter,
} from '@/components/ui/dialog'
import {Button} from '@/components/ui/button'
import {Checkbox} from '@/components/ui/checkbox'
import {Label} from '@/components/ui/label'
import {Role, User} from '@/types/usuarios/user'
import {ROLE_LABELS} from '@/constants/roles'
import {useAsyncAction} from '@/hooks/useAsyncAction'

interface Props {
    open: boolean
    usuario: User | null
    onOpenChange: (open: boolean) => void
    onSalvar: (usuarioId: number, perfis: Role[]) => Promise<void>
}

const roleLabels = (Object.entries(ROLE_LABELS) as [Role, string][]).map(([role, label]) => ({role, label}))

export function ModalEditarPerfis({open, usuario, onOpenChange, onSalvar}: Props) {
    const [selecionados, setSelecionados] = useState<Set<Role>>(
        () => (open && usuario ? new Set(usuario.perfis) : new Set()),
    )
    const [sincronizado, setSincronizado] = useState(() => ({open, usuarioId: usuario?.id ?? null}))
    const {executando, executar} = useAsyncAction({
        onClose: () => onOpenChange(false),
        mensagemPadrao: 'Erro ao atualizar perfis.',
    })

    // Ajusta o estado durante o render quando `open`/`usuario` mudam
    // (padrão recomendado pelo React em vez de setState em useEffect).
    // Evita vazamento de perfis entre usuários distintos ao reabrir o modal.
    const usuarioId = usuario?.id ?? null
    if (sincronizado.open !== open || sincronizado.usuarioId !== usuarioId) {
        setSincronizado({open, usuarioId})
        setSelecionados(open && usuario ? new Set(usuario.perfis) : new Set())
    }

    function toggle(role: Role) {
        if (role === Role.COLABORADOR) return
        setSelecionados((s) => {
            const novo = new Set(s)
            if (novo.has(role)) novo.delete(role)
            else novo.add(role)
            return novo
        })
    }

    function salvar() {
        if (!usuario) return
        const perfis = Array.from(new Set([...selecionados, Role.COLABORADOR]))
        void executar(() => onSalvar(usuario.id, perfis))
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Editar Perfis — {usuario?.nome}</DialogTitle>
                </DialogHeader>
                <div className="space-y-2">
                    {roleLabels.map((rl) => (
                        <div key={rl.role} className="flex items-center gap-2">
                            <Checkbox
                                id={rl.role}
                                checked={selecionados.has(rl.role)}
                                onCheckedChange={() => toggle(rl.role)}
                                disabled={rl.role === Role.COLABORADOR}
                            />
                            <Label htmlFor={rl.role}>{rl.label}</Label>
                        </div>
                    ))}
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={() => onOpenChange(false)}>Cancelar</Button>
                    <Button onClick={salvar} disabled={executando}>
                        {executando ? 'Salvando…' : 'Salvar'}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}
