import type {ReactNode} from 'react'
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog'
import {
    Drawer,
    DrawerContent,
    DrawerDescription,
    DrawerFooter,
    DrawerHeader,
    DrawerTitle,
} from '@/components/ui/drawer'
import {useIsMobile} from '@/hooks/useIsMobile'

interface ResponsiveModalProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    title: string
    description?: string
    children: ReactNode
    footer?: ReactNode
}

// Wrapper dos modais de edição (UC07–UC17, arquitetura §15.9): Dialog no
// desktop, Drawer no mobile (breakpoint `md`, via useIsMobile).
//
// Desktop — corpo rolante (decisão aprovada): o DialogContent usa
// `grid-rows-[auto_minmax(0,1fr)_auto]` para fixar header e footer e deixar
// apenas o corpo com scroll (`overflow-y-auto` + `min-h-0`, necessário para o
// grid child encolher). O `overflow-hidden` no content neutraliza a guarda
// global de `ui/dialog.tsx` via tailwind-merge.
//
// Mobile — o Drawer limita a altura a 85dvh; o corpo rola entre header e
// footer, e o footer respeita a safe-area inferior (iPhone).
export function ResponsiveModal({
                                    open,
                                    onOpenChange,
                                    title,
                                    description,
                                    children,
                                    footer,
                                }: ResponsiveModalProps) {
    const isMobile = useIsMobile()

    if (isMobile) {
        return (
            <Drawer open={open} onOpenChange={onOpenChange}>
                <DrawerContent className="max-h-[85dvh]">
                    <DrawerHeader>
                        <DrawerTitle>{title}</DrawerTitle>
                        {description && <DrawerDescription>{description}</DrawerDescription>}
                    </DrawerHeader>
                    <div className="flex-1 overflow-y-auto p-4">{children}</div>
                    {footer && (
                        <DrawerFooter className="pb-[env(safe-area-inset-bottom)]">{footer}</DrawerFooter>
                    )}
                </DrawerContent>
            </Drawer>
        )
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-2xl max-h-[85dvh] grid-rows-[auto_minmax(0,1fr)_auto] overflow-hidden">
                <DialogHeader>
                    <DialogTitle>{title}</DialogTitle>
                    {description && <DialogDescription>{description}</DialogDescription>}
                </DialogHeader>
                <div className="-mx-4 min-h-0 overflow-y-auto px-4">{children}</div>
                {footer && <DialogFooter>{footer}</DialogFooter>}
            </DialogContent>
        </Dialog>
    )
}