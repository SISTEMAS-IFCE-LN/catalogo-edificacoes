package br.edu.ifce.security.config

import br.edu.ifce.security.model.application.service.RefreshTokenService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Configuration
@EnableScheduling
class TokenCleanupScheduler(
    private val refreshTokenService: RefreshTokenService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 3 * * *")
    fun limparTokensExpirados() {
        val quantidade = refreshTokenService.limparTokensExpirados()
        log.info("Limpeza de refresh tokens expirados concluída. Tokens removidos: {}", quantidade)
    }
}
