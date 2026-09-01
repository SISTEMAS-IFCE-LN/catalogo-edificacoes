import { useNavigate } from 'react-router'
import { useState } from 'react'
import { Button } from '@/components/ui/button'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select'
import { useIsMobile } from '@/hooks/useIsMobile'
import { PAGES_ROUTES } from '@/constants/routes'

const OPCAO_NENHUMA = 'NENHUMA'

export interface AcaoLote {
    value: string
    onRun: (selectedIds: number[]) => void
}

interface AcoesLoteProps {
    selectedIds: number[]
    onClear: () => void
    /** Ações disponíveis; default é "Detalhar Esquadrias" (UC20-FE). */
    acoes?: AcaoLote[]
}

/**
 * Barra de ações em lote. Só renderiza quando há ao menos um item selecionado.
 * - Desktop (≥md): barra inline acima da tabela.
 * - Mobile (<md): barra fixa no rodapé com safe-area (arquitetura §15.7).
 *
 * Ações disponíveis (extensível para partes 09/10):
 * - "Detalhes Esquadrias" (UC20-FE): navega para /ambientes/publicados/esquadrias?ids=...
 */
export function AcoesLote({ selectedIds, onClear, acoes: acoesCustomizadas }: AcoesLoteProps) {
    const navigate = useNavigate()
    const isMobile = useIsMobile()
    const [acao, setAcao] = useState<string>(OPCAO_NENHUMA)

    if (selectedIds.length === 0) return null

    const acoes: AcaoLote[] = acoesCustomizadas ?? [
        {
            value: 'Detalhar Esquadrias',
            onRun: (ids) => {
                navigate(`${PAGES_ROUTES.PUBLICADOS}/esquadrias?ids=${ids.join(',')}`)
            },
        },
    ]

    function executar() {
        const selecionada = acoes.find((a) => a.value === acao)
        if (selecionada) {
            selecionada.onRun(selectedIds)
        }
    }

    const contador = (
        <span className="text-sm font-medium">
            {selectedIds.length} selecionado{selectedIds.length > 1 ? 's' : ''}
        </span>
    )

    const seletor = (
        <Select value={acao} onValueChange={(v) => setAcao(v ?? OPCAO_NENHUMA)}>
            <SelectTrigger
                className="w-50"
                aria-label="Selecionar ação em lote"
            >
                <SelectValue />
            </SelectTrigger>
            <SelectContent>
                <SelectItem value={OPCAO_NENHUMA}>Selecionar ação…</SelectItem>
                {acoes.map((a) => (
                    <SelectItem key={a.value} value={a.value}>
                        {a.value}
                    </SelectItem>
                ))}
            </SelectContent>
        </Select>
    )

    const botaoExecutar = (
        <Button
            size="sm"
            onClick={executar}
            disabled={acao === OPCAO_NENHUMA}
            className="min-h-11"
        >
            Executar
        </Button>
    )

    const botaoLimpar = (
        <Button
            variant="outline"
            size="sm"
            onClick={onClear}
            className="min-h-11"
        >
            Limpar
        </Button>
    )

    if (isMobile) {
        return (
            <div
                className="fixed bottom-0 inset-x-0 z-40 border-t bg-background p-4 space-y-3"
                style={{ paddingBottom: 'max(1rem, env(safe-area-inset-bottom))' }}
                role="region"
                aria-label="Ações em lote"
            >
                <div className="flex items-center justify-between">
                    {contador}
                    {botaoLimpar}
                </div>
                <div className="flex gap-2">
                    {seletor}
                    {botaoExecutar}
                </div>
            </div>
        )
    }

    return (
        <div
            className="flex items-center gap-2 rounded-lg border bg-muted/50 p-3"
            role="region"
            aria-label="Ações em lote"
        >
            {contador}
            {seletor}
            {botaoExecutar}
            {botaoLimpar}
        </div>
    )
}