package br.edu.ifce.ambientes_internos.model.domain.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("VAO_ABERTO")
class VaoAberto(
    geometria: Geometria,
    informacaoAdicional: String = ""
): Esquadria(
    id = null,
    TipoEsquadria.VAO_ABERTO,
    geometria,
    MaterialEsquadria.NAO_SE_APLICA,
    informacaoAdicional) {
}
