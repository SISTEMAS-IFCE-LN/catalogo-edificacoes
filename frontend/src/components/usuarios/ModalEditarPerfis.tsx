import {useState} from 'react'
import {
    Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter,
} from '@/components/ui/dialog'
import {Button} from '@/components/ui/button'
import {Checkbox} from '@/components/ui/checkbox'
import {Label} from '@/components/ui/label'
import {Role, User} from '@/types/user'
import {toast} from 'sonner'
import axios from 'axios'
import {ROLE_LABELS} from '@/constants/roles'

interface Props {
    open: boolean
    usuario: User | null
    onOpenChange: (open: boolean) => void
    onSalvar: (usuarioId: number, perfis: Role[]) => Promise<void>
}

interface RoleLabel {
    role: Role
    label: string
}

const roleLabels = Object.entries(ROLE_LABELS).map(([role, label]) => {
    return {role, label} as RoleLabel
})

export function ModalEditarPerfis({open, usuario, onOpenChange, onSalvar}: Props) {
    const [selecionados, setSelecionados] = useState<Set<Role>>(new Set())
    const [salvando, setSalvando] = useState(false)

    if (open && usuario && selecionados.size === 0) setSelecionados(new Set(usuario.perfis))

    function toggle(role: Role) {
        if (role === Role.COLABORADOR) return
        setSelecionados((s) => {
            const novo = new Set(s)
            if (novo.has(role)) novo.delete(role)
            else novo.add(role)
            return novo
        })
    }

    async function salvar() {
        if (!usuario) return
        const perfis = Array.from(new Set([...selecionados, Role.COLABORADOR]))
        setSalvando(true)
        try {
            await onSalvar(usuario.id, perfis)
            onOpenChange(false)
        } catch (e) {
            const status = axios.isAxiosError(e) ? e.response?.status : undefined
            if (status === 409) {
                toast.error('Não é possível remover o último administrador do sistema.')
            } else {
                toast.error('Erro ao atualizar perfis.')
            }
        } finally {
            setSalvando(false)
        }
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
                    <Button onClick={salvar} disabled={salvando}>
                        {salvando ? 'Salvando…' : 'Salvar'}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}