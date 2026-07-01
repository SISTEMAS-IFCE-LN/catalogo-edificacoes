package br.edu.ifce.common.exception

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ErroRes(
    val dataHora: String,
    val status: Int,
    val mensagem: String
) {
    companion object {
        private val FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        fun of(status: Int, mensagem: String): ErroRes =
            ErroRes(
                dataHora = LocalDateTime.now().format(FORMATO),
                status = status,
                mensagem = mensagem
            )
    }
}
