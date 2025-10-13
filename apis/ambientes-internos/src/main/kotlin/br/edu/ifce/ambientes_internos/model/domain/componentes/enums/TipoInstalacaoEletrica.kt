package br.edu.ifce.ambientes_internos.model.domain.componentes.enums

enum class TipoInstalacaoEletrica(val nome: String) {
    TOMADA_MONOFASICA("Tomada Monofásica"),
    TOMADA_TRIFASICA("Tomada Trifásica"),
    INTERRUPTOR_SIMPLES("Interruptor Simples"),
    INTERRUPTOR_DUPLO("Interruptor Duplo"),
    INTERRUPTOR_TRIPLO("Interruptor Triplo"),
    CHAVE_MAGNETICA("Chave Magnética"),
    QUADRO_DISTRIBUICAO("Quadro de Distribuição"),
    QUADRO_COMANDO("Quadro de Comando"),
    INVERSOR_TENSAO("Inversor de Tensão"),
    TRANSFORMADOR("Transformador")
}
