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
    fun limparTokensInuteis() {
        val quantidade = refreshTokenService.limparTokensInuteis()
        log.info("Limpeza de refresh tokens inúteis concluída. Tokens removidos: {}", quantidade)
    }
}
