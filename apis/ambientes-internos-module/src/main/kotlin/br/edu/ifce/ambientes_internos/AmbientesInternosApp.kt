package br.edu.ifce.ambientes_internos

// TODO Passo 5: mover esta classe para o módulo `main-app`.
// Mantida temporariamente aqui para que os testes @SpringBootTest deste
// módulo consigam localizar um @SpringBootApplication no classpath.
// Ver `docs/planejamento/sec-details-plan.md` (Passo 3 e Passo 5).
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AmbientesInternosApp

fun main(args: Array<String>) {
	runApplication<AmbientesInternosApp>(*args)
}
