package br.edu.ifce

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["br.edu.ifce.ambientes_internos", "br.edu.ifce.security"])
class CatalogoEdificacoesApp

fun main(args: Array<String>) {
	runApplication<CatalogoEdificacoesApp>(*args)
}
