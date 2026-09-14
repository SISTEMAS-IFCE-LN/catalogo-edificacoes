import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table'
import type {AmbienteDetalhe} from '@/types/ambientes/response'

const DECIMAIS_MEDIDA = 2

function formatarMedida(valor: number): string {
    return valor.toFixed(DECIMAIS_MEDIDA)
}

function formatarAndar(andar: number): string {
    return andar === 0 ? 'Térreo' : `${andar}º Andar`
}

function obterDimensoesAmbiente(base: number, altura: number): { comprimento: number; largura: number } {
    return {
        comprimento: Math.max(base, altura),
        largura: Math.min(base, altura),
    }
}

export function DetalheAmbiente({ambiente}: { ambiente: AmbienteDetalhe }) {
    const areaTotalEsquadrias = ambiente.esquadriasDetalhes.esquadrias.reduce(
        (total, esquadria) => total + esquadria.area,
        0,
    )

    return (
        <div className="space-y-6">
            <div>
                <h2 className="text-xl font-semibold">{ambiente.nome}</h2>
                <p className="text-sm">
                    Tipo: {ambiente.tipo}
                </p>
                <p className="text-sm">
                    Capacidade: {ambiente.capacidade} Pessoas
                </p>
                <p className="text-sm">
                    Localização: {ambiente.localizacao.unidade} • {ambiente.localizacao.bloco} • {formatarAndar(ambiente.localizacao.andar)}
                </p>
                {ambiente.informacaoAdicional && <p className="mt-2 text-sm">{ambiente.informacaoAdicional}</p>}
            </div>

            <section>
                <h3 className="font-medium">
                    Pés-direitos: <span
                    className="text-sm">{ambiente.pesDireitos.map((peDireito) => `${peDireito}m`).join(', ')}</span>
                </h3>
            </section>

            <section>
                <h3 className="font-medium">Dimensões</h3>
                <Table aria-label="Dimensões">
                    <TableHeader>
                        <TableRow>
                            <TableHead>Formato</TableHead>
                            <TableHead>Comprimento</TableHead>
                            <TableHead>Largura</TableHead>
                            <TableHead>Repetição</TableHead>
                            <TableHead>Área</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {ambiente.geometrias.map((geometria) => {
                            const dimensoes = obterDimensoesAmbiente(geometria.base, geometria.altura)

                            return (
                                <TableRow key={geometria.id}>
                                    <TableCell>{geometria.tipo}</TableCell>
                                    <TableCell>{formatarMedida(dimensoes.comprimento)}</TableCell>
                                    <TableCell>{formatarMedida(dimensoes.largura)}</TableCell>
                                    <TableCell>{geometria.repeticao}</TableCell>
                                    <TableCell>{formatarMedida(geometria.area)}</TableCell>
                                </TableRow>
                            )
                        })}
                        <TableRow className="font-medium">
                            <TableCell colSpan={4}>Área Total do Ambiente</TableCell>
                            <TableCell>{formatarMedida(ambiente.areaAmbiente)}</TableCell>
                        </TableRow>
                    </TableBody>
                </Table>
            </section>

            <section>
                <h3 className="font-medium">Esquadrias</h3>
                <Table aria-label="Esquadrias">
                    <TableHeader>
                        <TableRow>
                            <TableHead>Tipo</TableHead>
                            <TableHead>Material</TableHead>
                            <TableHead>Comprimento</TableHead>
                            <TableHead>Altura</TableHead>
                            <TableHead>Peitoril</TableHead>
                            <TableHead>Repetição</TableHead>
                            <TableHead>Área</TableHead>
                            <TableHead>Informação Adicional</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {ambiente.esquadriasDetalhes.esquadrias.map((esquadria) => {
                            const dimensoes = {
                                comprimento: esquadria.geometria.base,
                                altura: esquadria.geometria.altura,
                            }

                            return (
                                <TableRow key={esquadria.id}>
                                    <TableCell>{esquadria.tipo}</TableCell>
                                    <TableCell>{esquadria.material}</TableCell>
                                    <TableCell>{formatarMedida(dimensoes.comprimento)}</TableCell>
                                    <TableCell>{formatarMedida(dimensoes.altura)}</TableCell>
                                    <TableCell>{formatarMedida(esquadria.alturaPeitoril)}</TableCell>
                                    <TableCell>{esquadria.geometria.repeticao}</TableCell>
                                    <TableCell>{formatarMedida(esquadria.area)}</TableCell>
                                    <TableCell>{esquadria.informacaoAdicional}</TableCell>
                                </TableRow>
                            )
                        })}
                        <TableRow className="font-medium">
                            <TableCell colSpan={6}>Área Total das Esquadrias</TableCell>
                            <TableCell>{formatarMedida(areaTotalEsquadrias)}</TableCell>
                            <TableCell/>
                        </TableRow>
                    </TableBody>
                </Table>
            </section>
        </div>
    )
}
