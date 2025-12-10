package br.edu.ifce.ambientes_internos

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Auditorio
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaAdministrativa
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaAula
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Janela
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.Porta
import br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums.MaterialEsquadria
import br.edu.ifce.ambientes_internos.model.domain.geometrias.Retangular
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

@Configuration
class Runner(val ambienteRepository: AmbienteRepository) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        val janela1 = Janela(
            geometria = Retangular(base = BigDecimal("1.00"), altura = BigDecimal("1.50")), // 1.50
            material = MaterialEsquadria.ALUMINIO,
            alturaPeitoril = BigDecimal("0.90")
        )

        val janela2 = Janela(
            geometria = Retangular(base = BigDecimal("0.80"), altura = BigDecimal("1.40")), // 1.12
            material = MaterialEsquadria.ALUMINIO,
            alturaPeitoril = BigDecimal("0.90")
        )

        val porta1 = Porta(
            geometria = Retangular(base = BigDecimal("0.90"), altura = BigDecimal("2.00")), // 1.80
            material = MaterialEsquadria.MADEIRA_MACICA
        )

        val salaAula = SalaAula(
            nome = "Sala de Aula Modelo",
            localizacao = Localizacao(Bloco.BLOCO_11, Unidade.SEDE, 2),
            capacidade = 50,
            pesDireitos = mutableSetOf(2.5.toBigDecimal(), 3.0.toBigDecimal()),
            esquadrias = mutableSetOf(janela1, janela2, porta1)
        )

        val salaAdministrativa = SalaAdministrativa(
            nome = "Sala Administrativa",
            localizacao = Localizacao(Bloco.BLOCO_4, Unidade.SEDE),
            capacidade = 5
        )

        val auditorio = Auditorio(
            nome = "Auditório Principal",
            localizacao = Localizacao(Bloco.BLOCO_5, Unidade.SEDE),
            capacidade = 200,
            pesDireitos = mutableSetOf(3.5.toBigDecimal()),
            informacaoAdicional = "Auditório para grandes eventos e palestras."
        )

        val salaAulaSalva = ambienteRepository.save(salaAula)
        val salaAdmSalva = ambienteRepository.save(salaAdministrativa)
        val auditorioSalvo = ambienteRepository.save(auditorio)

        println(listOf(salaAulaSalva, salaAdmSalva, auditorioSalvo))

        println(ambienteRepository.findById(1L).get())
    }

}