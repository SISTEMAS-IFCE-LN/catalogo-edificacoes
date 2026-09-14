import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
    DialogDescription,
} from '@/components/ui/dialog'
import {Button} from '@/components/ui/button'
import {useAsyncAction} from '@/hooks/useAsyncAction'

interface Props {
    open: boolean
    title: string
    description?: string
    onConfirm: () => Promise<void>
    onOpenChange: (open: boolean) => void
    variant?: 'default' | 'destructive'
    confirmLabel?: string
}

export function ModalConfirmacao({
                                     open,
                                     title,
                                     description,
                                     onConfirm,
                                     onOpenChange,
                                     variant = 'default',
                                     confirmLabel = 'Confirmar',
                                 }: Props) {
    const {executando, executar} = useAsyncAction({
        onClose: () => onOpenChange(false),
    })

    function confirmar() {
        void executar(onConfirm)
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>{title}</DialogTitle>
                    {description && <DialogDescription>{description}</DialogDescription>}
                </DialogHeader>
                <DialogFooter>
                    <Button variant="outline" onClick={() => onOpenChange(false)}>
                        Cancelar
                    </Button>
                    <Button variant={variant} onClick={confirmar} disabled={executando}>
                        {executando ? 'Executando…' : confirmLabel}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}
