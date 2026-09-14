import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table'
import type {
    AmbienteEsquadrias,
    EsquadriaTipoMaterial,
    EsquadriasResponse,
} from '@/types/ambientes/esquadrias'

const DECIMAIS_MEDIDA = 2

function formatarMedida(valor: number): string {
    return valor.toFixed(DECIMAIS_MEDIDA)
}

function formatarAndar(andar: number): string {
    return andar === 0 ? 'Térreo' : `${andar}º Andar`
}

function ResumoTipoMaterial({ itens }: { itens: EsquadriaTipoMaterial[] }) {
    if (itens.length === 0) return null

    return (
        <div className="text-sm">
            <strong>Resumo:</strong>
            <ul className="list-disc pl-6">
                {itens.map((r) => (
                    <li key={`${r.tipo}-${r.material}`}>
                        {r.tipo} / {r.material}: {formatarMedida(r.area)} m²
                    </li>
                ))}
            </ul>
        </div>
    )
}

function CardEsquadriasAmbiente({ ambiente }: { ambiente: AmbienteEsquadrias }) {
    const { dadosAmbiente, detalhesEsquadrias } = ambiente

    return (
        <div className="border rounded-lg p-4 space-y-3">
            <div>
                <h2 className="text-lg font-semibold">{dadosAmbiente.nome}</h2>
                <p className="text-sm text-muted-foreground">
                    {dadosAmbiente.localizacao.unidade} • {dadosAmbiente.localizacao.bloco} •{' '}
                    {formatarAndar(dadosAmbiente.localizacao.andar)}
                </p>
            </div>

            {detalhesEsquadrias.esquadrias.length > 0 ? (
                <Table aria-label={`Esquadrias de ${dadosAmbiente.nome}`}>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Tipo</TableHead>
                            <TableHead>Largura</TableHead>
                            <TableHead>Altura</TableHead>
                            <TableHead>Material</TableHead>
                            <TableHead>Peitoril</TableHead>
                            <TableHead>Repetição</TableHead>
                            <TableHead>Área</TableHead>
                            <TableHead>Informação Adicional</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {detalhesEsquadrias.esquadrias.map((e) => (
                            <TableRow key={e.id}>
                                <TableCell>{e.tipo}</TableCell>
                                <TableCell>{formatarMedida(e.geometria.base)}</TableCell>
                                <TableCell>{formatarMedida(e.geometria.altura)}</TableCell>
                                <TableCell>{e.material}</TableCell>
                                <TableCell>
                                    {e.alturaPeitoril > 0 ? formatarMedida(e.alturaPeitoril) : '—'}
                                </TableCell>
                                <TableCell>{e.geometria.repeticao}</TableCell>
                                <TableCell>{formatarMedida(e.area)}</TableCell>
                                <TableCell>{e.informacaoAdicional || '—'}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            ) : (
                <p className="text-sm text-muted-foreground">
                    Nenhuma esquadria para os filtros aplicados.
                </p>
            )}

            <ResumoTipoMaterial itens={detalhesEsquadrias.esquadriasTipoMaterial} />
        </div>
    )
}

export function DetalheEsquadrias({ response }: { response: EsquadriasResponse }) {
    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold">Detalhes de Esquadrias</h1>

            {response.ambientes.map((amb) => (
                <CardEsquadriasAmbiente key={amb.dadosAmbiente.id} ambiente={amb} />
            ))}

            {response.totalTipoMaterial.length > 0 && (
                <div className="border-t pt-4 space-y-2">
                    <h2 className="text-lg font-semibold">Resumo Global</h2>
                    <ul className="text-sm list-disc pl-6">
                        {response.totalTipoMaterial.map((r) => (
                            <li key={`${r.tipo}-${r.material}`}>
                                {r.tipo} / {r.material}: {formatarMedida(r.area)} m²
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    )
}