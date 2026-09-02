import {z} from 'zod'

// Enums espelhados do backend (Kotlin)
// Fonte: apis/ambientes-internos-module/src/main/kotlin/br/edu/ifce/ambientes_internos/model/domain/entity/ambientes/enums/

export enum TipoAmbiente {
    SALA_AULA = 'Sala de Aula',
    LABORATORIO = 'Laboratório',
    LABORATORIO_INFORMATICA = 'Laboratório de Informática',
    BANHEIRO = 'Banheiro',
    VESTIARIO = 'Vestiário',
    CIRCULACAO = 'Circulação',
    AUDITORIO = 'Auditório',
    SALA_REUNIAO = 'Sala de Reunião',
    SALA_PROFESSORES = 'Sala de Professores',
    SALA_ADMINISTRATIVA = 'Sala Administrativa',
    SALA_COORDENACAO = 'Sala de Coordenação',
    SALA_SERVIDOR_TI = 'Sala de Servidor de TI',
    COZINHA = 'Cozinha',
    BIBLIOTECA = 'Biblioteca',
    GINASIO = 'Ginásio',
    ACADEMIA = 'Academia',
    CANTINA = 'Cantina',
    DEPOSITO = 'Depósito',
}

export enum Bloco {
    BLOCO_1 = 'Bloco 1',
    BLOCO_2 = 'Bloco 2',
    BLOCO_3 = 'Bloco 3',
    BLOCO_4 = 'Bloco 4',
    BLOCO_5 = 'Bloco 5',
    BLOCO_6 = 'Bloco 6',
    BLOCO_7 = 'Bloco 7',
    BLOCO_8 = 'Bloco 8',
    BLOCO_9 = 'Bloco 9',
    BLOCO_10 = 'Bloco 10',
    BLOCO_11 = 'Bloco 11',
    BLOCO_12 = 'Bloco 12',
    PATIO = 'Pátio',
    BLOCO_PRINCIPAL = 'Bloco Principal',
    BIBLIOTECA_ACADEMICA = 'Biblioteca/ Acadêmica',
    GINASIO = 'Ginásio',
    REFEITORIO = 'Refeitório',
    ENCUBADORA = 'Incubadora',
    GALPAO = 'Galpão',
    CASA_MEL = 'Casa de Mel',
    OUTRO = 'Outro',
}

export enum Unidade {
    SEDE = 'Sede',
    CIDADE_ALTA = 'Cidade Alta',
    UEPE = 'UEPE',
}

export enum TipoFiltro {
    NENHUM = '',
    NOME = 'nome',
    TIPO = 'tipo',
    LOCALIZACAO = 'localizacao',
}

export enum TipoEsquadria {
    PORTA = "Porta",
    JANELA = "Janela",
    COBOGO = "Cobogó",
    VAO_ABERTO = "Vão Aberto",
    ESQUADRIA_OUTRO_AMBIENTE = "Esquadria de outro ambiente"
}

export enum TipoGeometria {
    RETANGULAR = "Retangular",
    TRIANGULAR = "Triangular"
}

export enum MaterialEsquadria {
    ALUMINIO = "Alumínio",
    ALUMINIO_VIDRO = "Alumínio e Vidro",
    ALUMINIO_PVC = "Alumínio e PVC",
    FERRO = "Ferro",
    FERRO_VIDRO = "Ferro e Vidro",
    VIDRO = "Vidro",
    PVC = "PVC",
    MADEIRA_MACICA = "Madeira Maciça",
    MADEIRA_VIDRO = "Madeira e Vidro",
    MADEIRA_VENEZIANA = "Madeira Tipo Veneziana",
    MADEIRA_FICHA = "Madeira Tipo Ficha",
    MADEIRA_PARANA = "Madeira Tipo Paraná",
    PRE_MOLDADO = "Pré-Moldado",
    NAO_SE_APLICA = "Não se Aplica",
    OUTRO = "Outro"
}

export enum StatusAmbiente {
    PUBLICADO = "Publicado",
    NAO_PUBLICADO = "Não Publicado",
    AGUARDANDO_VALIDACAO = "Aguardando Validação"
}

// Os DTOs de detalhe retornam os nomes dos enums Kotlin (por exemplo,
// `RETANGULAR`), enquanto a lista retorna os rótulos (`Retangular`).
// Normalizamos ambos para os valores exibidos pelo frontend.
export const TipoGeometriaResponseSchema = z.union([
    z.enum(TipoGeometria),
    z.enum(['RETANGULAR', 'TRIANGULAR']),
]).transform((value) => (
    value in TipoGeometria
        ? TipoGeometria[value as keyof typeof TipoGeometria]
        : value
))

export const TipoEsquadriaResponseSchema = z.union([
    z.enum(TipoEsquadria),
    z.enum(['PORTA', 'JANELA', 'COBOGO', 'VAO_ABERTO', 'ESQUADRIA_OUTRO_AMBIENTE']),
]).transform((value) => (
    value in TipoEsquadria
        ? TipoEsquadria[value as keyof typeof TipoEsquadria]
        : value
))

export const MaterialEsquadriaResponseSchema = z.union([
    z.enum(MaterialEsquadria),
    z.enum([
        'ALUMINIO',
        'ALUMINIO_VIDRO',
        'ALUMINIO_PVC',
        'FERRO',
        'FERRO_VIDRO',
        'VIDRO',
        'PVC',
        'MADEIRA_MACICA',
        'MADEIRA_VIDRO',
        'MADEIRA_VENEZIANA',
        'MADEIRA_FICHA',
        'MADEIRA_PARANA',
        'PRE_MOLDADO',
        'NAO_SE_APLICA',
        'OUTRO',
    ]),
]).transform((value) => (
    value in MaterialEsquadria
        ? MaterialEsquadria[value as keyof typeof MaterialEsquadria]
        : value
))

export const StatusAmbienteResponseSchema = z.union([
    z.enum(StatusAmbiente),
    z.enum(['PUBLICADO', 'NAO_PUBLICADO', 'AGUARDANDO_VALIDACAO']),
]).transform((value) => (
    value in StatusAmbiente
        ? StatusAmbiente[value as keyof typeof StatusAmbiente]
        : value
))