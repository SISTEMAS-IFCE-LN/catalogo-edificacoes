package br.edu.ifce.ambientes_internos.model.domain.factory

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Academia
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Auditorio
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Banheiro
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Biblioteca
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Cantina
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Circulacao
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Cozinha
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Deposito
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ginasio
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Laboratorio
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.LaboratorioInformatica
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.SalaAdministrativa
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.SalaAula
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.SalaCoordenacao
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.SalaProfessores
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.SalaReuniao
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.SalaServidorTi
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Vestiario
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteReq
import java.math.BigDecimal

object AmbienteFactory {

    private val AMBIENTES_MAP = mapOf<TipoAmbiente, (
        String,
        br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Localizacao,
        Int,
        MutableSet<br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Geometria>,
        MutableSet<BigDecimal>,
        MutableSet<br.edu.ifce.ambientes_internos.model.domain.entity.esquadrias.Esquadria>,
        String
    ) -> br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Ambiente>(
        TipoAmbiente.SALA_AULA to ::SalaAula,
        TipoAmbiente.LABORATORIO to ::Laboratorio,
        TipoAmbiente.LABORATORIO_INFORMATICA to ::LaboratorioInformatica,
        TipoAmbiente.BANHEIRO to ::Banheiro,
        TipoAmbiente.VESTIARIO to ::Vestiario,
        TipoAmbiente.CIRCULACAO to ::Circulacao,
        TipoAmbiente.AUDITORIO to ::Auditorio,
        TipoAmbiente.SALA_REUNIAO to ::SalaReuniao,
        TipoAmbiente.SALA_PROFESSORES to ::SalaProfessores,
        TipoAmbiente.SALA_ADMINISTRATIVA to ::SalaAdministrativa,
        TipoAmbiente.SALA_COORDENACAO to ::SalaCoordenacao,
        TipoAmbiente.SALA_SERVIDOR_TI to ::SalaServidorTi,
        TipoAmbiente.COZINHA to ::Cozinha,
        TipoAmbiente.BIBLIOTECA to ::Biblioteca,
        TipoAmbiente.GINASIO to ::Ginasio,
        TipoAmbiente.ACADEMIA to ::Academia,
        TipoAmbiente.CANTINA to ::Cantina,
        TipoAmbiente.DEPOSITO to ::Deposito
    )

    fun criar(ambienteReq: AmbienteReq): Ambiente {
        val construtor = AMBIENTES_MAP[ambienteReq.tipo]
            ?: throw IllegalArgumentException("O tipo de ambiente é inválido.")
        return construtor(
            ambienteReq.nome,
            Localizacao(
                unidade = ambienteReq.localizacao.unidade,
                bloco = ambienteReq.localizacao.bloco,
                andar = ambienteReq.localizacao.andar
            ),
            ambienteReq.capacidade,
            ambienteReq.geometrias.map { GeometriaFactory.criar(it) }.toMutableSet(),
            ambienteReq.pesDireitos.toMutableSet(),
            ambienteReq.esquadrias.map { EsquadriaFactory.criar(it) }.toMutableSet(),
            ambienteReq.informacaoAdicional
        )
    }

}