package br.edu.ifce.ambientes_internos.model.domain

import java.text.Normalizer
import java.util.Locale

/**
 * Fonte única da normalização de nomes de localização (Campus, Unidade e Bloco).
 *
 * O valor persistido é sempre a forma normalizada; a exibição também usa essa forma.
 * A normalização é aplicada no construtor e no setter — o Hibernate, ao hidratar,
 * escreve o campo diretamente (o valor já persistido é normalizado).
 */
object NormalizadorNome {

    private val MARCAS = Regex("\\p{Mn}+")
    private val SEPARADORES = Regex("[^\\p{L}\\p{Nd}]+")
    private val DIGITOS = Regex("\\d+")
    private val ESPACOS = Regex("\\s+")

    fun normalizar(valor: String): String = valor
        .trim()
        .let { Normalizer.normalize(it, Normalizer.Form.NFD) }
        .replace(MARCAS, "")
        .uppercase(Locale.ROOT)
        .replace(SEPARADORES, " ")
        .replace(DIGITOS) { m -> m.value.trimStart('0').ifEmpty { "0" } }
        .replace(ESPACOS, " ")
        .trim()
}
