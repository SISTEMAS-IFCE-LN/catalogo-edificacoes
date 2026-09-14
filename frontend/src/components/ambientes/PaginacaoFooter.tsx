import { Button } from '@/components/ui/button'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select'

const TAMANHOS_PAGINA = [10, 20, 50, 100]

interface PaginacaoFooterProps {
    page: number
    size: number
    areaTotal: number
    hasPrevious: boolean
    hasNext: boolean
    currentPage: number
    totalPages: number
    onPageChange: (page: number) => void
    onSizeChange: (size: string | null) => void
}

/**
 * Rodapé de paginação + Área Total, compartilhado pelas listas de ambientes
 * (publicados, validação e não-publicados).
 */
export function PaginacaoFooter({
    page,
    size,
    areaTotal,
    hasPrevious,
    hasNext,
    currentPage,
    totalPages,
    onPageChange,
    onSizeChange,
}: PaginacaoFooterProps) {
    return (
        <div className="flex items-center justify-between gap-4">
            <span className="text-sm text-muted-foreground">
                Área Total: {areaTotal.toFixed(2)} m²
            </span>
            <div className="flex items-center gap-2">
                <span className="text-sm text-muted-foreground">Itens por página:</span>
                <Select value={String(size)} onValueChange={onSizeChange}>
                    <SelectTrigger className="w-[70px]" aria-label="Itens por página">
                        <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                        {TAMANHOS_PAGINA.map((t) => (
                            <SelectItem key={t} value={String(t)}>
                                {t}
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>
            </div>
            <div className="flex gap-2">
                <Button
                    variant="outline"
                    size="sm"
                    disabled={!hasPrevious}
                    onClick={() => onPageChange(page - 1)}
                >
                    Anterior
                </Button>
                <span className="text-sm flex items-center">
                    Página {currentPage + 1} de {totalPages}
                </span>
                <Button
                    variant="outline"
                    size="sm"
                    disabled={!hasNext}
                    onClick={() => onPageChange(page + 1)}
                >
                    Próximo
                </Button>
            </div>
        </div>
    )
}
