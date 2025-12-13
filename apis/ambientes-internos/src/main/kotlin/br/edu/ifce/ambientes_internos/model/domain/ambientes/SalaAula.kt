package br.edu.ifce.ambientes_internos.model.domain.ambientes

import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import java.math.BigDecimal
import jakarta.persistence.Entity
import jakarta.persistence.DiscriminatorValue

@Entity
@DiscriminatorValue("SALA_AULA")
class SalaAula(
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
    TipoAmbiente.SALA_AULA,
    geometrias,
    pesDireitos,
    esquadrias,
    informacaoAdicional,
    StatusAmbiente.NAO_PUBLICADO
) {}
