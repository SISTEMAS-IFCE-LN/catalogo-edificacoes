package br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias

import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.enums.TipoEsquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Geometria
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("PORTA")
class Porta(
    geometria: Geometria,
    material: MaterialEsquadria,
    informacaoAdicional: String = ""
): Esquadria(
    id = null,
    TipoEsquadria.PORTA,
    geometria,
    material,
    informacaoAdicional) {
}
