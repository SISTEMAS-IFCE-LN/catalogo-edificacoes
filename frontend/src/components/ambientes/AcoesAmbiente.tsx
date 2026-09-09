import { useState } from 'react'
import type { ReactNode } from 'react'
import { Button } from '@/components/ui/button'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select'
import { usePermission } from '@/hooks/usePermission'

const OPCAO_NENHUMA = 'Selecionar ação…'

export interface AcaoAmbiente {
    /** Rótulo exibido no Select. */
    value: string
    /** Chave em ACTION_PERMISSIONS (ex.: 'ambiente:editar'). */
    actionKey: string
    onRun: () => void
}

interface AcoesAmbienteProps {
    /** Ações não-críticas (opções do Select), filtradas por permissão. */
    acoes: AcaoAmbiente[]
    /** Slot direito: ações críticas como botões diretos (ex.: Deletar). */
    criticalActions?: ReactNode
}

/**
 * Barra de ações do detalhe de um ambiente (1 registro — sempre renderiza,
 * diferente do AcoesLote). Fica no topo da página: o flex-wrap empilha os
 * grupos no mobile, sem barra fixa no rodapé (arquitetura §15.11).
 */
export function AcoesAmbiente({ acoes, criticalActions }: AcoesAmbienteProps) {
    const { canDo } = usePermission()
    const [acao, setAcao] = useState<string>(OPCAO_NENHUMA)

    // Opções visíveis apenas para quem tem a permissão da ação.
    const acoesPermitidas = acoes.filter((a) => canDo(a.actionKey))

    function executar() {
        const selecionada = acoesPermitidas.find((a) => a.value === acao)
        if (selecionada) {
            selecionada.onRun()
        }
        // Reseta para evitar reexecução acidental da mesma ação.
        setAcao(OPCAO_NENHUMA)
    }

    return (
        <div
            className="flex flex-wrap items-center justify-between gap-2 rounded-lg border bg-muted/50 p-3"
            role="region"
            aria-label="Ações do ambiente"
        >
            <div className="flex flex-wrap items-center gap-2">
                <span className="text-sm font-medium">Ações do ambiente</span>
                {/* Altura: 44px no mobile (§15.12), altura nativa no desktop (size-11 md:size-8). */}
                <Select value={acao} onValueChange={(v) => setAcao(v ?? OPCAO_NENHUMA)}>
                    <SelectTrigger
                        className="w-50 min-h-11 md:min-h-0"
                        aria-label="Selecionar ação"
                    >
                        <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value={OPCAO_NENHUMA} className="min-h-11 md:min-h-0">
                            Selecionar ação…
                        </SelectItem>
                        {acoesPermitidas.map((a) => (
                            <SelectItem key={a.value} value={a.value} className="min-h-11 md:min-h-0">
                                {a.value}
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>
                <Button
                    onClick={executar}
                    disabled={acao === OPCAO_NENHUMA}
                    className="min-h-11 md:min-h-0"
                >
                    Executar
                </Button>
                <Button
                    variant="outline"
                    onClick={() => setAcao(OPCAO_NENHUMA)}
                    className="min-h-11 md:min-h-0"
                >
                    Limpar
                </Button>
            </div>
            {criticalActions && (
                <div className="flex flex-wrap items-center gap-2">{criticalActions}</div>
            )}
        </div>
    )
}
