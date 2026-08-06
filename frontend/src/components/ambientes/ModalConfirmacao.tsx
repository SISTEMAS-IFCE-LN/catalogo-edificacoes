import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
    DialogDescription,
} from '@/components/ui/dialog'
import {Button} from '@/components/ui/button'
import {useState} from 'react'
import {toast} from 'sonner'

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
    const [executando, setExecutando] = useState(false)

    async function confirmar() {
        setExecutando(true)
        try {
            await onConfirm()
            onOpenChange(false)
        } catch (error) {
            toast.error('Erro ao executar ação. Tente novamente.')
            console.error('Erro ao confirmar:', error)
        } finally {
            setExecutando(false)
        }
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
