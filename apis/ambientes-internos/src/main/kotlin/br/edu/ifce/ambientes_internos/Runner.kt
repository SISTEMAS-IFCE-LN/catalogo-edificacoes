package br.edu.ifce.ambientes_internos

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Auditorio
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaAdministrativa
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaAula
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Configuration

@Configuration
class Runner(val ambienteRepository: AmbienteRepository) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        val salaAula = SalaAula(
            nome = "Sala de Aula Modelo",
            localizacao = Localizacao(Bloco.BLOCO_11, Unidade.SEDE, 2),
            capacidade = 50,
            pesDireitos = mutableSetOf(2.5.toBigDecimal(), 3.0.toBigDecimal())
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
    }

}