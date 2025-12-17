package br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Geometria
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("ESQUADRIA_OUTRO_AMBIENTE")
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
