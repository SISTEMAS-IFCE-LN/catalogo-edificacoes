package br.edu.ifce.security.integracao

import br.edu.ifce.security.TestApplication
import br.edu.ifce.security.model.application.service.CustomOAuth2UserService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

/**
 * Regressão: `CustomOAuth2UserService` não deve ser proxiado por AOP.
 *
 * A transação de banco vive em `UsuarioOAuth2Service`, então este bean não recebe advice e o
 * `CglibAopProxy` deixa de avisar que `setRestOperations`/`setRequestEntityConverter` (final em
 * `DefaultOAuth2UserService`) não podem ser proxiados. Também garante que a chamada HTTP ao
 * provedor OAuth2 fique fora de transação.
 */
@SpringBootTest(classes = [TestApplication::class])
@ActiveProfiles("test")
@Import(CustomOAuth2UserService::class)
class CustomOAuth2UserServiceAopTest {

    @Autowired
    lateinit var customOAuth2UserService: CustomOAuth2UserService

    @Test
    fun `nao deve ser proxiado por AOP`() {
        assertFalse(AopUtils.isAopProxy(customOAuth2UserService))
    }
}
