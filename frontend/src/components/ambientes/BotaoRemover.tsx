import {Trash2Icon} from 'lucide-react'
import {Button} from '@/components/ui/button'

export function BotaoRemover({ariaLabel, onClick}: { ariaLabel: string; onClick: () => void }) {
    return (
        <Button type="button" variant="ghost" size="icon" aria-label={ariaLabel} onClick={onClick}>
            <Trash2Icon/>
        </Button>
    )
}
