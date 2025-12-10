package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria

class EsquadriaOutroAmbiente(
    geometria: Geometria,
    informacaoAdicional: String = ""
): Esquadria(
    id = null,
    TipoEsquadria.ESQUADRIA_OUTRO_AMBIENTE,
    geometria,
    MaterialEsquadria.NAO_SE_APLICA,
    informacaoAdicional) {
}
