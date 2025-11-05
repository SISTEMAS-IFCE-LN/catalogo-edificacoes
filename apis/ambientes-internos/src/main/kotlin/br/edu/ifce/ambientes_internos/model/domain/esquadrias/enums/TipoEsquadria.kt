package br.edu.ifce.ambientes_internos.model.domain.esquadrias.enums

enum class TipoEsquadria(val nome: String) {
    ALUMINIO("Alumínio"),
    ALUMINIO_VIDRO("Alumínio e Vidro"),
    ALUMINIO_PVC("Alumínio e PVC"),
    FERRO("Ferro"),
    FERRO_VIDRO("Ferro e Vidro"),
    VIDRO("Vidro"),
    PVC("PVC"),
    MADEIRA_MACICA("Madeira Maciça"),
    MADEIRA_VIDRO("Madeira e Vidro"),
    MADEIRA_VENEZIANA("Madeira Tipo Veneziana"),
    MADEIRA_FICHA("Madeira Tipo Ficha"),
    MADEIRA_PARANA("Madeira Tipo Paraná"),
    VAO_ABERTO("Vão Aberto"),
    ACESSO_OUTRO_AMBIENTE("Acesso a outro ambiente"),
    OUTRO("Outro")
}
