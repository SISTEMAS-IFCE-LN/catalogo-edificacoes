package br.edu.ifce.ambientes_internos

import br.edu.ifce.ambientes_internos.model.domain.ambientes.Auditorio
import br.edu.ifce.ambientes_internos.model.domain.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaAdministrativa
import br.edu.ifce.ambientes_internos.model.domain.ambientes.SalaAula
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

    }

}