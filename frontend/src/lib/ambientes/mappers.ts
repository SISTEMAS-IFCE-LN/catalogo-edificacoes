import {nomeTecnicoDeRotulo} from '@/types/ambientes/request'
import type {
    AmbienteInput,
    DadosBasicosInput,
    DuplicacaoInput,
    EsquadriaInput,
    GeometriaInput,
    LocalizacaoInput,
} from '@/types/ambientes/request'
import type {AmbienteDetalhe} from '@/types/ambientes/response'
import {
    Bloco,
    MaterialEsquadria,
    TipoAmbiente,
    TipoEsquadria,
    TipoGeometria,
    Unidade,
} from '@/types/ambientes/enums'

// Conversão resposta → request para os modais de edição (UC07–UC17). Os DTOs
// de detalhe devolvem RÓTULOS ('Bloco 1', 'Retangular', 'Porta'...), enquanto
// os DTOs de request exigem NOMES TÉCNICOS ('BLOCO_1', 'RETANGULAR',
// 'PORTA'...) — ver plano 11 §4 e arquitetura §13. nomeTecnicoDeRotulo devolve
// string; os casts restringem ao union do enum correspondente.

function localizacaoDeDetalhe(ambiente: AmbienteDetalhe): LocalizacaoInput {
    return {
        bloco: nomeTecnicoDeRotulo(Bloco, ambiente.localizacao.bloco) as LocalizacaoInput['bloco'],
        unidade: nomeTecnicoDeRotulo(Unidade, ambiente.localizacao.unidade) as LocalizacaoInput['unidade'],
        andar: ambiente.localizacao.andar,
    }
}

// UC07-FE — PATCH /{id}/dados-basicos
export function dadosBasicosDeDetalhe(ambiente: AmbienteDetalhe): DadosBasicosInput {
    return {
        nome: ambiente.nome,
        capacidade: ambiente.capacidade,
        localizacao: localizacaoDeDetalhe(ambiente),
    }
}

// UC17-FE — POST /{id}/duplicar (nome/localizacao ORIGINAIS, sem sufixo)
export function duplicacaoDeDetalhe(ambiente: AmbienteDetalhe): DuplicacaoInput {
    return {
        nome: ambiente.nome,
        localizacao: localizacaoDeDetalhe(ambiente),
    }
}

// UC09-FE — PATCH /{id}/geometrias/atualizar
export function geometriasDeDetalhe(ambiente: AmbienteDetalhe): GeometriaInput[] {
    return ambiente.geometrias.map((g) => ({
        tipo: nomeTecnicoDeRotulo(TipoGeometria, g.tipo) as GeometriaInput['tipo'],
        base: g.base,
        altura: g.altura,
        repeticao: g.repeticao,
    }))
}

// UC13-FE — PATCH /{id}/esquadrias/atualizar
export function esquadriasDeDetalhe(ambiente: AmbienteDetalhe): EsquadriaInput[] {
    return ambiente.esquadriasDetalhes.esquadrias.map((e) => ({
        tipo: nomeTecnicoDeRotulo(TipoEsquadria, e.tipo) as EsquadriaInput['tipo'],
        material: nomeTecnicoDeRotulo(MaterialEsquadria, e.material) as EsquadriaInput['material'],
        geometria: {
            base: e.geometria.base,
            altura: e.geometria.altura,
            repeticao: e.geometria.repeticao,
        },
        alturaPeitoril: e.alturaPeitoril,
        informacaoAdicional: e.informacaoAdicional,
    }))
}

// UC16-FE — POST /{id} recebe um AmbienteReq COMPLETO (tipo + nome +
// localizacao + capacidade + geometrias + pesDireitos + esquadrias + info
// adicional — AmbienteNaoPublicadoController.kt:133-144), não apenas o tipo.
export function ambienteDeDetalhe(ambiente: AmbienteDetalhe): AmbienteInput {
    return {
        nome: ambiente.nome,
        tipo: nomeTecnicoDeRotulo(TipoAmbiente, ambiente.tipo) as AmbienteInput['tipo'],
        capacidade: ambiente.capacidade,
        localizacao: localizacaoDeDetalhe(ambiente),
        geometrias: geometriasDeDetalhe(ambiente),
        pesDireitos: ambiente.pesDireitos,
        esquadrias: esquadriasDeDetalhe(ambiente),
        informacaoAdicional: ambiente.informacaoAdicional,
    }
}
