package br.edu.ifce.ambientes_internos.model.domain.ambientes

import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal
import jakarta.persistence.Entity
import jakarta.persistence.DiscriminatorValue

@Entity
@DiscriminatorValue("CIRCULACAO")
class Circulacao(
    nome: String,
    localizacao: String,
    capacidade: Int,
    geometrias: MutableSet<Geometria> = mutableSetOf(),
    pesDireitos: MutableSet<BigDecimal> = mutableSetOf(),
    esquadrias: MutableSet<Esquadria> = mutableSetOf(),
    informacaoAdicional: String = ""
) : Ambiente(
    id = null,
    nome,
    localizacao,
    TipoAmbiente.CIRCULACAO,
    capacidade,
    geometrias,
    pesDireitos,
    esquadrias,
    informacaoAdicional,
    StatusAmbiente.NAO_PUBLICADO
) {}
