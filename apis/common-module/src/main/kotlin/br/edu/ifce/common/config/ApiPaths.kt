package br.edu.ifce.common.config

object ApiPaths {
    const val AMBIENTES_PATH = "/api/ambientes"
    const val AMBIENTES_NAO_PUBLICADOS_PATH = "${AMBIENTES_PATH}/nao-publicados"
    const val AMBIENTES_PUBLICADOS_PATH = "${AMBIENTES_PATH}/publicados"
    const val AMBIENTES_VALIDACAO_PATH = "${AMBIENTES_PATH}/validacao"

    const val USUARIOS_PATH = "/api/usuarios"

    const val AUTH_PATH = "/auth"
}