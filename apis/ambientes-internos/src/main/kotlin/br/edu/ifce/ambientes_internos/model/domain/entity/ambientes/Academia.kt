package br.edu.ifce.ambientes_internos.model.domain.entity.ambientes

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Geometria
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.math.BigDecimal

@Entity
@DiscriminatorValue("ACADEMIA")
class Academia(
    nome: String,
    localizacao: Localizacao,
    capacidade: Int,
    geometrias: MutableSet<Geometria> = mutableSetOf(),
    pesDireitos: MutableSet<BigDecimal> = mutableSetOf(),
    esquadrias: MutableSet<Esquadria> = mutableSetOf(),
    informacaoAdicional: String = ""
) : Ambiente(
    id = null,
    nome,
    localizacao,
    capacidade,
    TipoAmbiente.ACADEMIA,
    geometrias,
    pesDireitos,
    esquadrias,
    informacaoAdicional,
    StatusAmbiente.NAO_PUBLICADO
) {}
