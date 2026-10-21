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
    MaterialEsquadria,
    TipoAmbiente,
    TipoEsquadria,
    TipoGeometria,
} from '@/types/ambientes/enums'

// Conversão resposta → request para os modais de edição (UC07–UC17).
// Localização usa {id, nome: null}: o id pré-preenche os selects; o nome fica
// nulo para o backend usar a entidade existente. Os demais enums continuam
// precisando de nomeTecnicoDeRotulo (rótulos na resposta, chaves no request).

function localizacaoDeDetalhe(ambiente: AmbienteDetalhe): LocalizacaoInput {
    return {
        campus: {id: ambiente.localizacao.campus.id, nome: null},
        unidade: {id: ambiente.localizacao.unidade.id, nome: null},
        bloco: {id: ambiente.localizacao.bloco.id, nome: null},
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
