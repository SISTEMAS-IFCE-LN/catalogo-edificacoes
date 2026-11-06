package br.edu.ifce.ambientes_internos.model.domain.factory

import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Localizacao
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoReq

/**
 * Monta a hierarquia transitória usada como "carrier" de referências.
 *
 * - `id` preenchido  -> representa entidade existente; `nome` fica vazio e é ignorado.
 * - `nome` preenchido -> representa entidade nova; `id` fica nulo.
 *
 * Nada aqui é persistido: o use case resolve a hierarquia antes do save.
 */
object LocalizacaoFactory {

    fun criar(req: LocalizacaoReq): Localizacao {
        val campus = Campus(
            nomeInicial = req.campus.nome ?: "",
            id = req.campus.id
        )
        val unidade = Unidade(
            campus = campus,
            nomeInicial = req.unidade.nome ?: "",
            id = req.unidade.id
        )
        val bloco = Bloco(
            unidade = unidade,
            nomeInicial = req.bloco.nome ?: "",
            id = req.bloco.id
        )
        return Localizacao(bloco = bloco, andar = req.andar)
    }
}
