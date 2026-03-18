package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.application.interfaces.IAmbienteValidacaoUseCases
import br.edu.ifce.ambientes_internos.model.application.usecases.AmbienteValidacaoUseCases
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.LaboratorioInformatica
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.SalaAula
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.StatusAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.TipoAmbiente
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.enums.Unidade
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Geometria
import br.edu.ifce.ambientes_internos.model.domain.entity.geometrias.Retangular
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoPesquisaReq
import br.edu.ifce.ambientes_internos.model.repository.AmbienteRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

@DataJpaTest
@Import(AmbienteValidacaoUseCases::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Testes de integração para os Casos de Uso dos Ambientes em Validação")
class AmbienteValidacaoUseCasesIntegrationTest {

    @Autowired
    lateinit var ambientesVUseCases: IAmbienteValidacaoUseCases

    @Autowired
    lateinit var repoAmb: AmbienteRepository

    // -------------------------------------------------------------------------
    // Helpers de criação de entidades
    // -------------------------------------------------------------------------

    private fun criarSalaAula(
        nome: String,
        bloco: Bloco,
        unidade: Unidade,
        andar: Int,
        capacidade: Int,
        base: String,
        altura: String,
        status: StatusAmbiente = StatusAmbiente.AGUARDANDO_VALIDACAO,
        informacaoAdicional: String = ""
    ): SalaAula {
        val ambiente = SalaAula(
            nome = nome,
            localizacao = Localizacao(
                bloco = bloco,
                unidade = unidade,
                andar = andar
            ),
            capacidade = capacidade,
            geometrias = mutableSetOf(
                Retangular(base = BigDecimal(base), altura = BigDecimal(altura)) as Geometria
            ),
            pesDireitos = mutableSetOf(BigDecimal("3.0")),
            informacaoAdicional = informacaoAdicional
        )
        ambiente.status = status
        return repoAmb.save(ambiente)
    }

    private fun criarLaboratorioInformatica(
        nome: String,
        bloco: Bloco,
        unidade: Unidade,
        andar: Int,
        capacidade: Int,
        base: String,
        altura: String,
        status: StatusAmbiente = StatusAmbiente.AGUARDANDO_VALIDACAO,
        informacaoAdicional: String = ""
    ): LaboratorioInformatica {
        val ambiente = LaboratorioInformatica(
            nome = nome,
            localizacao = Localizacao(
                bloco = bloco,
                unidade = unidade,
                andar = andar
            ),
            capacidade = capacidade,
            geometrias = mutableSetOf(
                Retangular(base = BigDecimal(base), altura = BigDecimal(altura)) as Geometria
            ),
            pesDireitos = mutableSetOf(BigDecimal("3.2")),
            informacaoAdicional = informacaoAdicional
        )
        ambiente.status = status
        return repoAmb.save(ambiente)
    }

    // -------------------------------------------------------------------------
    // Testes
    // -------------------------------------------------------------------------

    @Test
    fun `Deve obter ambiente em validacao por id`() {
        val ambienteSalvo = criarSalaAula(
            nome = "Sala em validação",
            bloco = Bloco.BLOCO_10,
            unidade = Unidade.CIDADE_ALTA,
            andar = 1,
            capacidade = 30,
            base = "6.0",
            altura = "3.0",
            informacaoAdicional = "Ambiente aguardando validação"
        )

        val ambienteRecuperado = ambientesVUseCases.obterAmbientePorId(ambienteSalvo.id!!)

        assertEquals(ambienteSalvo.id, ambienteRecuperado.id)
        assertEquals(ambienteSalvo.nome, ambienteRecuperado.nome)
        assertEquals(StatusAmbiente.AGUARDANDO_VALIDACAO, ambienteRecuperado.status)
        assertEquals(ambienteSalvo.capacidade, ambienteRecuperado.capacidade)
    }

    @Test
    fun `Deve lancar excecao ao obter ambiente fora de validacao por id`() {
        val ambienteSalvo = criarSalaAula(
            nome = "Sala não publicada",
            bloco = Bloco.BLOCO_11,
            unidade = Unidade.CIDADE_ALTA,
            andar = 1,
            capacidade = 25,
            base = "5.0",
            altura = "3.0",
            status = StatusAmbiente.NAO_PUBLICADO
        )

        assertThrows<NoSuchElementException> {
            ambientesVUseCases.obterAmbientePorId(ambienteSalvo.id!!)
        }
    }

    @Test
    fun `Deve listar ambientes em validacao com paginacao`() {
        criarSalaAula(
            nome = "Sala em validação 1",
            bloco = Bloco.BLOCO_10,
            unidade = Unidade.CIDADE_ALTA,
            andar = 1,
            capacidade = 30,
            base = "6.0",
            altura = "3.0"
        )
        criarSalaAula(
            nome = "Sala em validação 2",
            bloco = Bloco.BLOCO_11,
            unidade = Unidade.CIDADE_ALTA,
            andar = 2,
            capacidade = 35,
            base = "4.0",
            altura = "3.0"
        )
        criarSalaAula(
            nome = "Sala publicada",
            bloco = Bloco.BLOCO_12,
            unidade = Unidade.CIDADE_ALTA,
            andar = 3,
            capacidade = 40,
            base = "8.0",
            altura = "3.0",
            status = StatusAmbiente.PUBLICADO
        )

        val resultado = ambientesVUseCases.listarAmbientes(PageRequest.of(0, 10))

        assertEquals(2, resultado.ambientes.size)
        assertEquals(2, resultado.dadosPaginacao.totalElements)
        assertEquals(1, resultado.dadosPaginacao.totalPages)
        assertEquals(0, resultado.dadosPaginacao.currentPage)
        assertEquals(false, resultado.dadosPaginacao.hasNext)
        assertEquals(false, resultado.dadosPaginacao.hasPrevious)
        assertEquals(BigDecimal("30.00"), resultado.areaTotal)
    }

    @Test
    fun `Deve listar ambientes em validacao por tipo`() {
        criarSalaAula(
            nome = "Sala em validação",
            bloco = Bloco.BLOCO_10,
            unidade = Unidade.CIDADE_ALTA,
            andar = 1,
            capacidade = 30,
            base = "6.0",
            altura = "3.0"
        )
        criarLaboratorioInformatica(
            nome = "Laboratório em validação",
            bloco = Bloco.BLOCO_11,
            unidade = Unidade.CIDADE_ALTA,
            andar = 2,
            capacidade = 25,
            base = "8.0",
            altura = "4.0"
        )
        criarSalaAula(
            nome = "Sala publicada",
            bloco = Bloco.BLOCO_12,
            unidade = Unidade.CIDADE_ALTA,
            andar = 3,
            capacidade = 40,
            base = "7.0",
            altura = "3.0",
            status = StatusAmbiente.PUBLICADO
        )

        val resultado = ambientesVUseCases.listarAmbientesPorTipo(
            TipoAmbiente.SALA_AULA.name,
            PageRequest.of(0, 10)
        )

        assertEquals(1, resultado.ambientes.size)
        assertEquals(1, resultado.dadosPaginacao.totalElements)
        assertEquals("Sala em validação", resultado.ambientes.first().nome)
        assertEquals(Bloco.BLOCO_10, resultado.ambientes.first().localizacao.bloco)
    }

    @Test
    fun `Deve listar ambientes em validacao por nome`() {
        criarSalaAula(
            nome = "Sala de Aula em validação",
            bloco = Bloco.BLOCO_10,
            unidade = Unidade.CIDADE_ALTA,
            andar = 1,
            capacidade = 30,
            base = "6.0",
            altura = "3.0"
        )
        criarSalaAula(
            nome = "Laboratório de Química",
            bloco = Bloco.BLOCO_11,
            unidade = Unidade.CIDADE_ALTA,
            andar = 2,
            capacidade = 20,
            base = "5.0",
            altura = "3.0"
        )

        val resultado = ambientesVUseCases.listarAmbientesPorNome(
            "sala de aula",
            PageRequest.of(0, 10)
        )

        assertEquals(1, resultado.ambientes.size)
        assertEquals(1, resultado.dadosPaginacao.totalElements)
        assertEquals("Sala de Aula em validação", resultado.ambientes.first().nome)
    }

    @Test
    fun `Deve listar ambientes em validacao por localizacao`() {
        criarSalaAula(
            nome = "Sala no bloco 10",
            bloco = Bloco.BLOCO_10,
            unidade = Unidade.CIDADE_ALTA,
            andar = 1,
            capacidade = 30,
            base = "6.0",
            altura = "3.0"
        )
        criarSalaAula(
            nome = "Sala no bloco 11",
            bloco = Bloco.BLOCO_11,
            unidade = Unidade.CIDADE_ALTA,
            andar = 2,
            capacidade = 35,
            base = "5.0",
            altura = "3.0"
        )
        criarSalaAula(
            nome = "Sala fora da validação",
            bloco = Bloco.BLOCO_10,
            unidade = Unidade.CIDADE_ALTA,
            andar = 3,
            capacidade = 28,
            base = "4.0",
            altura = "3.0",
            status = StatusAmbiente.PUBLICADO
        )

        val resultado = ambientesVUseCases.listarAmbientesPorLocalizacao(
            LocalizacaoPesquisaReq(bloco = "BLOCO_10", unidade = null, andar = null),
            PageRequest.of(0, 10)
        )

        assertEquals(1, resultado.ambientes.size)
        assertEquals(1, resultado.dadosPaginacao.totalElements)
        assertEquals("Sala no bloco 10", resultado.ambientes.first().nome)
    }

    @Test
    fun `Deve limitar pagina para no maximo 100 ambientes em validacao`() {
        (1..120).forEach { indice ->
            criarSalaAula(
                nome = "Sala validacao $indice",
                bloco = Bloco.BLOCO_10,
                unidade = Unidade.CIDADE_ALTA,
                andar = indice,
                capacidade = 30,
                base = "6.0",
                altura = "3.0"
            )
        }

        val resultado = ambientesVUseCases.listarAmbientes(PageRequest.of(0, 1000))

        assertEquals(120, resultado.dadosPaginacao.totalElements)
        assertEquals(100, resultado.dadosPaginacao.pageSize)
        assertEquals(100, resultado.ambientes.size)
    }

    @Test
    fun `Deve publicar ambiente em validacao`() {
        val ambienteSalvo = criarSalaAula(
            nome = "Sala pronta para publicação",
            bloco = Bloco.BLOCO_10,
            unidade = Unidade.CIDADE_ALTA,
            andar = 1,
            capacidade = 30,
            base = "6.0",
            altura = "3.0"
        )

        ambientesVUseCases.publicarAmbiente(ambienteSalvo.id!!)
        val ambienteAtualizado = repoAmb.findById(ambienteSalvo.id!!).orElseThrow()

        assertEquals(StatusAmbiente.PUBLICADO, ambienteAtualizado.status)
        assertThrows<NoSuchElementException> {
            ambientesVUseCases.obterAmbientePorId(ambienteSalvo.id!!)
        }
    }

    @Test
    fun `Deve privar ambiente em validacao`() {
        val ambienteSalvo = criarSalaAula(
            nome = "Sala para retornar a não publicado",
            bloco = Bloco.BLOCO_10,
            unidade = Unidade.CIDADE_ALTA,
            andar = 1,
            capacidade = 30,
            base = "6.0",
            altura = "3.0"
        )

        ambientesVUseCases.privarAmbiente(ambienteSalvo.id!!)
        val ambienteAtualizado = repoAmb.findById(ambienteSalvo.id!!).orElseThrow()

        assertEquals(StatusAmbiente.NAO_PUBLICADO, ambienteAtualizado.status)
        assertThrows<NoSuchElementException> {
            ambientesVUseCases.obterAmbientePorId(ambienteSalvo.id!!)
        }
    }

    @Test
    fun `Deve privar ambiente publicado`() {
        val ambienteSalvo = criarSalaAula(
            nome = "Sala publicada para privar",
            bloco = Bloco.BLOCO_10,
            unidade = Unidade.CIDADE_ALTA,
            andar = 1,
            capacidade = 30,
            base = "6.0",
            altura = "3.0",
            status = StatusAmbiente.PUBLICADO
        )

        ambientesVUseCases.privarAmbiente(ambienteSalvo.id!!)
        val ambienteAtualizado = repoAmb.findById(ambienteSalvo.id!!).orElseThrow()

        assertEquals(StatusAmbiente.NAO_PUBLICADO, ambienteAtualizado.status)
    }

    @Test
    fun `Deve lancar excecao ao publicar ambiente inexistente`() {
        assertThrows<NoSuchElementException> {
            ambientesVUseCases.publicarAmbiente(Long.MAX_VALUE)
        }
    }

    @Test
    fun `Deve lancar excecao ao privar ambiente inexistente`() {
        assertThrows<NoSuchElementException> {
            ambientesVUseCases.privarAmbiente(Long.MAX_VALUE)
        }
    }
}
