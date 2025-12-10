package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("COBOGO")
class Cobogo(
    geometria: Geometria,
    informacaoAdicional: String = ""
): Esquadria(
    id = null,
    TipoEsquadria.COBOGO,
    geometria,
    MaterialEsquadria.PRE_MOLDADO,
    informacaoAdicional) {
}
