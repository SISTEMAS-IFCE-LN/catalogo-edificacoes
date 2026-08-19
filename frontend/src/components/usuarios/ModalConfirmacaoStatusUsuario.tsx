import {
    Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter,
} from '@/components/ui/dialog'
import {Button} from '@/components/ui/button'
import type {User, StatusAcao} from '@/types/usuarios/user'
import {useAsyncAction} from '@/hooks/useAsyncAction'

interface Props {
    open: boolean
    usuario: User | null
    acao: StatusAcao
    onConfirmar: () => Promise<void>
    onOpenChange: (open: boolean) => void
}

export function ModalConfirmacaoStatusUsuario({open, usuario, acao, onConfirmar, onOpenChange}: Props) {
    const {executando, executar} = useAsyncAction({
        onClose: () => onOpenChange(false),
        mensagemPadrao: 'Erro ao alterar status do usuário.',
    })

    function confirmar() {
        void executar(onConfirmar)
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>
                        {acao === 'desativar' ? 'Desativar' : 'Ativar'} {usuario?.nome}?
                    </DialogTitle>
                </DialogHeader>
                <DialogFooter>
                    <Button variant="outline" onClick={() => onOpenChange(false)}>Cancelar</Button>
                    <Button variant={acao === 'desativar' ? 'destructive' : 'default'} onClick={confirmar} disabled={executando}>
                        {executando ? 'Executando…' : 'Confirmar'}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}
