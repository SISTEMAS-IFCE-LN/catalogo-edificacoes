package br.edu.ifce.ambientes_internos

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Auditorio
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaAdministrativa
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaAula
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Configuration

@Configuration
class Runner(val ambienteRepository: AmbienteRepository): ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        val salaAula = SalaAula(
            nome = "Sala de Aula Modelo",
            localizacao = "Bloco 11 - 2º Andar",
            capacidade = 50,
            pesDireitos = mutableSetOf(2.5.toBigDecimal(), 3.0.toBigDecimal())
        )

        val salaAdministrativa = SalaAdministrativa(
            nome = "Sala Administrativa",
            localizacao = "Bloco 4",
            capacidade = 5
        )

        val auditorio = Auditorio(
            nome = "Auditório Principal",
            localizacao = "Bloco Central",
            capacidade = 200,
            pesDireitos = mutableSetOf(3.5.toBigDecimal()),
            informacaoAdicional = "Auditório para grandes eventos e palestras."
        )

        ambienteRepository.save(salaAula)
        ambienteRepository.save(salaAdministrativa)
        ambienteRepository.save(auditorio)
    }

}