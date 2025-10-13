package br.edu.ifce.ambientes_internos.model.domain.elementos_construtivos.enums

enum class TipoTeto(val nome: String) {
    LAJE_PLANA_CONCRETO("Laje plana de concreto"),
    LAJE_PLANA_PREFABRICADA("Laje plana pré-fabricada"),
    LAJE_INCLINADA_CONCRETO("Laje inclinada de concreto"),
    LAJE_INCLINADA_PREFABRICADA("Laje inclinada pré-fabricada"),
    FORRO_GESSO("Forro de gesso"),
    FORRO_GESSO_ACARTONADO("Forro de gesso acartonado"),
    FORRO_MADEIRA("Forro de madeira"),
    FORRO_PVC("Forro de PVC"),
    TELHADO("Telhado"),
    OUTRO("Outro")
}
