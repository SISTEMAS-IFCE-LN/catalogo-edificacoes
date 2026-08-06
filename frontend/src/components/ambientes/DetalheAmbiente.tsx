import type { AmbienteDetalhe } from '@/types/ambientes/ambiente'

export function DetalheAmbiente({ ambiente }: { ambiente: AmbienteDetalhe }) {
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold">{ambiente.nome}</h2>
        <p className="text-sm text-muted-foreground">
          Bloco {ambiente.localizacao.bloco} • Unidade {ambiente.localizacao.unidade} • Andar {ambiente.localizacao.andar}
        </p>
        <p className="text-sm">Tipo: {ambiente.tipo} • Capacidade: {ambiente.capacidade} • Área: {ambiente.areaAmbiente.toFixed(2)} m²</p>
        {ambiente.informacaoAdicional && <p className="text-sm mt-2">{ambiente.informacaoAdicional}</p>}
      </div>

      <div>
        <h3 className="font-medium">Geometrias</h3>
        <ul className="text-sm list-disc pl-6">
          {ambiente.geometrias.map((g) => (
            <li key={g.id}>
              {g.tipo} — {g.base}x{g.altura}m — área {g.area.toFixed(2)} m² (repetição {g.repeticao})
            </li>
          ))}
        </ul>
      </div>

      <div>
        <h3 className="font-medium">Pés-direitos</h3>
        <p className="text-sm">{ambiente.pesDireitos.map((p) => `${p}m`).join(', ')}</p>
      </div>

      <div>
        <h3 className="font-medium">Esquadrias</h3>
        <ul className="text-sm list-disc pl-6">
          {ambiente.esquadriasDetalhes.esquadrias.map((e) => (
            <li key={e.id}>
              {e.tipo} {e.geometria.base}x{e.geometria.altura}m — {e.material} (área {e.area.toFixed(2)} m²)
              {e.alturaPeitoril > 0 && <span> — peitoril: {e.alturaPeitoril}m</span>}
              {e.informacaoAdicional && <span> — {e.informacaoAdicional}</span>}
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}
