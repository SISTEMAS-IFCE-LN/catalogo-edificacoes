package br.edu.ifce.ambientes_internos.model.domain.entity.ambientes

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Geometria
import java.math.BigDecimal
import jakarta.persistence.Entity
import jakarta.persistence.DiscriminatorValue

@Entity
@DiscriminatorValue("LABORATORIO_INFORMATICA")
class LaboratorioInformatica(
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
    TipoAmbiente.LABORATORIO_INFORMATICA,
    geometrias,
    pesDireitos,
    esquadrias,
    informacaoAdicional,
    StatusAmbiente.NAO_PUBLICADO
) {}
