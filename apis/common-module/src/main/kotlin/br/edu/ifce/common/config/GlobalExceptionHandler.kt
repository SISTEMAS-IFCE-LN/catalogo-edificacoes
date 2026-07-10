package br.edu.ifce.common.config

import br.edu.ifce.common.dto.ErroRes
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): ResponseEntity<ErroRes> {
        val mensagem = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
            ?: "Corpo da requisição inválido."
        log.debug("MethodArgumentNotValidException: {}", mensagem)
        return responderErro(HttpStatus.BAD_REQUEST, mensagem)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErroRes> {
        val mensagem = ex.constraintViolations.firstOrNull()?.message
            ?: "Parâmetros inválidos."
        log.debug("ConstraintViolationException: {}", mensagem)
        return responderErro(HttpStatus.BAD_REQUEST, mensagem)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ErroRes> {
        log.debug("HttpMessageNotReadableException: {}", ex.message)
        return responderErro(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou mal formatado.")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErroRes> {
        log.debug("IllegalArgumentException: {}", ex.message)
        return responderErro(HttpStatus.BAD_REQUEST, ex.message ?: "Argumento inválido.")
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(ex: NoSuchElementException): ResponseEntity<ErroRes> {
        log.debug("NoSuchElementException: {}", ex.message)
        return responderErro(HttpStatus.NOT_FOUND, ex.message ?: "Recurso não encontrado.")
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<ErroRes> {
        log.debug("ResponseStatusException: {} - {}", ex.statusCode, ex.reason)
        val mensagem = ex.reason ?: ex.statusCode.toString()
        return ResponseEntity.status(ex.statusCode)
            .body(ErroRes.of(ex.statusCode.value(), mensagem))
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ErroRes> {
        log.debug("HttpRequestMethodNotSupportedException: {}", ex.message)
        val mensagem = buildString {
            append("Método HTTP não suportado: ").append(ex.method)
            ex.supportedHttpMethods?.takeIf { it.isNotEmpty() }
                ?.let { append(". Métodos suportados: ").append(it) }
        }
        return responderErro(HttpStatus.METHOD_NOT_ALLOWED, mensagem)
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupported(ex: HttpMediaTypeNotSupportedException): ResponseEntity<ErroRes> {
        log.debug("HttpMediaTypeNotSupportedException: {}", ex.message)
        val mensagem = buildString {
            append("Tipo de mídia não suportado: ").append(ex.contentType)
            if (ex.supportedMediaTypes.isNotEmpty()) {
                append(". Tipos suportados: ").append(ex.supportedMediaTypes)
            }
        }
        return responderErro(HttpStatus.UNSUPPORTED_MEDIA_TYPE, mensagem)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(ex: MissingServletRequestParameterException): ResponseEntity<ErroRes> {
        log.debug("MissingServletRequestParameterException: {}", ex.message)
        val mensagem = "Parâmetro de requisição '${ex.parameterName}' do tipo '${ex.parameterType}' está ausente."
        return responderErro(HttpStatus.BAD_REQUEST, mensagem)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(ex: DataIntegrityViolationException): ResponseEntity<ErroRes> {
        val mensagem = ex.mostSpecificCause?.message ?: ex.message ?: "Violação de integridade de dados."
        log.debug("DataIntegrityViolationException: {}", mensagem)
        return responderErro(HttpStatus.BAD_REQUEST, mensagem)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ErroRes> {
        log.debug("Acesso negado: {}", ex.message)
        return responderErro(HttpStatus.FORBIDDEN, "Acesso negado: você não tem permissão para acessar este recurso.")
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDenied(ex: AuthorizationDeniedException): ResponseEntity<ErroRes> {
        log.debug("Autorização negada: {}", ex.message)
        return responderErro(HttpStatus.FORBIDDEN, "Acesso negado: você não tem permissão para realizar esta ação.")
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErroRes> {
        log.error("Erro inesperado", ex)
        return responderErro(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Serviço indisponível no momento. Tente novamente mais tarde."
        )
    }

    private fun responderErro(status: HttpStatus, mensagem: String): ResponseEntity<ErroRes> =
        ResponseEntity.status(status).body(ErroRes.of(status.value(), mensagem))
}