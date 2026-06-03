package br.edu.ifce.ambientes_internos.model.dto

import org.springframework.data.domain.Page

data class DadosPaginacao(
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    companion object {
        fun from(page: Page<*>): DadosPaginacao {
            return DadosPaginacao(
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                currentPage = page.number,
                pageSize = page.size,
                hasNext = page.hasNext(),
                hasPrevious = page.hasPrevious()
            )
        }
    }
}
