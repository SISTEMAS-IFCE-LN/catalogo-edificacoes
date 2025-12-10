package br.edu.ifce.ambientes_internos.model.domain.ambientes.enums

enum class StatusAmbiente(val nome: String) {
    PUBLICADO("Publicado"),
    NAO_PUBLICADO("Não Publicado"),
    AGUARDANDO_VALIDACAO("Aguardando Validação")
}