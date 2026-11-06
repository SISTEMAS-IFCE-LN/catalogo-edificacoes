package br.edu.ifce.ambientes_internos.integracao

import br.edu.ifce.ambientes_internos.model.domain.NormalizadorNome
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Bloco
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Campus
import br.edu.ifce.ambientes_internos.model.domain.entity.ambientes.Unidade
import br.edu.ifce.ambientes_internos.model.dto.ambiente.LocalizacaoReq
import br.edu.ifce.ambientes_internos.model.dto.ambiente.ReferenciaReq
import br.edu.ifce.ambientes_internos.model.repository.BlocoRepository
import br.edu.ifce.ambientes_internos.model.repository.CampusRepository
import br.edu.ifce.ambientes_internos.model.repository.UnidadeRepository

/**
 * Helpers compartilhados pelos testes da hierarquia Campus -> Unidade -> Bloco.
 *
 * A persistência é idempotente (find-or-create) porque o contexto H2 é compartilhado
 * entre as classes de teste.
 */
fun criarCampus(
    repoCampus: CampusRepository,
    nome: String = "LIMOEIRO DO NORTE"
): Campus = repoCampus.findByNome(NormalizadorNome.normalizar(nome))
    ?: repoCampus.saveAndFlush(Campus(nome))

fun criarUnidade(
    repoCampus: CampusRepository,
    repoUnidade: UnidadeRepository,
    campusNome: String = "LIMOEIRO DO NORTE",
    unidadeNome: String = "CIDADE ALTA"
): Unidade {
    val campus = criarCampus(repoCampus, campusNome)
    val nomeNormalizado = NormalizadorNome.normalizar(unidadeNome)
    return repoUnidade.findByCampusAndNome(campus, nomeNormalizado)
        ?: repoUnidade.saveAndFlush(Unidade(campus, unidadeNome))
}

fun criarBloco(
    repoBloco: BlocoRepository,
    unidade: Unidade,
    nome: String
): Bloco = repoBloco.findByUnidadeAndNome(unidade, NormalizadorNome.normalizar(nome))
    ?: repoBloco.saveAndFlush(Bloco(unidade, nome))

fun refCampus(campus: Campus): ReferenciaReq = ReferenciaReq(id = campus.id)

fun refUnidade(unidade: Unidade): ReferenciaReq = ReferenciaReq(id = unidade.id)

fun refBloco(bloco: Bloco): ReferenciaReq = ReferenciaReq(id = bloco.id)

fun refNome(nome: String): ReferenciaReq = ReferenciaReq(nome = nome)

fun localizacaoReq(
    campus: Campus,
    unidade: Unidade,
    bloco: Bloco,
    andar: Int = 0
): LocalizacaoReq = LocalizacaoReq(
    campus = refCampus(campus),
    unidade = refUnidade(unidade),
    bloco = refBloco(bloco),
    andar = andar
)

fun localizacaoReqPorNome(
    campus: String = "LIMOEIRO DO NORTE",
    unidade: String = "CIDADE ALTA",
    bloco: String = "BLOCO 10",
    andar: Int = 0
): LocalizacaoReq = LocalizacaoReq(
    campus = refNome(campus),
    unidade = refNome(unidade),
    bloco = refNome(bloco),
    andar = andar
)
