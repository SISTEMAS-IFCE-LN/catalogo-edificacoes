package br.edu.ifce.security.model.application.service

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

/**
 * Carrega o usuário no provedor OAuth2 e delega a sincronização com o banco para
 * [UsuarioOAuth2Service] (transação curta, somente banco).
 *
 * Importante: esta classe não deve receber anotações que disparem proxy AOP (ex.: @Transactional).
 * `DefaultOAuth2UserService` possui setters finais herdados, e o proxy CGLIB emitiria avisos de
 * que eles não podem ser proxiados; além disso a transação envolveria a chamada HTTP ao provedor.
 */
@Service
class CustomOAuth2UserService(
    private val usuarioOAuth2Service: UsuarioOAuth2Service
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val email = oAuth2User.attributes["email"] as String?
            ?: throw OAuth2AuthenticationException(OAuth2Error("missing_email"), "Email não fornecido pelo provedor Google.")
        val nome = oAuth2User.attributes["name"] as String?
            ?: throw OAuth2AuthenticationException(OAuth2Error("missing_name"), "Nome não fornecido pelo provedor Google.")

        val authorities = usuarioOAuth2Service.sincronizar(email, nome)

        return DefaultOAuth2User(authorities, oAuth2User.attributes, "email")
    }
}
