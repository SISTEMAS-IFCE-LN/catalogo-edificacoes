package br.edu.ifce.ambientes_internos.model.domain.ambientes.factories

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Academia
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Ambiente
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Auditorio
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Banheiro
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Biblioteca
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Cantina
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Circulacao
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Cozinha
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Deposito
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Ginasio
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Laboratorio
import br.edu.ifce.ambientes_internos.model.domain.ambientes.LaboratorioInformatica
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaAdministrativa
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaAula
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaCoordenacao
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaProfessores
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaReuniao
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaServidorTi
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Vestiario
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Esquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Geometria
import br.edu.ifce.ambientes_internos.model.dto.ambiente.AmbienteReq
import java.math.BigDecimal

object AmbienteFactory {

    private val AMBIENTES_MAP = mapOf<TipoAmbiente, (
        String,
        Localizacao,
        Int,
        MutableSet<Geometria>,
        MutableSet<BigDecimal>,
        MutableSet<Esquadria>,
        String
    ) -> Ambiente>(
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