package br.edu.ifce.ambientes_internos.model.application.usecases

/**
 * Ordem natural: "BLOCO 2" < "BLOCO 2A" < "BLOCO 10" < "CASA".
 * `ORDER BY nome` é lexicográfico; por isso a ordenação é feita em memória.
 */
object ComparadorNatural {

    private val TOKENS = Regex("\\d+|\\D+")

    fun comparar(a: String, b: String): Int {
        val tokensA = TOKENS.findAll(a).map { it.value }.toList()
        val tokensB = TOKENS.findAll(b).map { it.value }.toList()

        for (i in 0 until minOf(tokensA.size, tokensB.size)) {
            val comparacao = compararToken(tokensA[i], tokensB[i])
            if (comparacao != 0) return comparacao
        }
        return tokensA.size - tokensB.size
    }

    private fun compararToken(a: String, b: String): Int =
        if (a.first().isDigit() && b.first().isDigit()) compararNumerico(a, b)
        else a.compareTo(b)

    private fun compararNumerico(a: String, b: String): Int =
        if (a.length != b.length) a.length - b.length
        else a.compareTo(b)
}
