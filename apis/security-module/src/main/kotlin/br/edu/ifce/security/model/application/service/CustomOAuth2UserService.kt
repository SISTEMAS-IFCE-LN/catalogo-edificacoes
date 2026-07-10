package br.edu.ifce.security.model.application.service

import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.domain.Usuario
import br.edu.ifce.security.model.repository.UsuarioRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOAuth2UserService(
    private val usuarioRepository: UsuarioRepository
) : DefaultOAuth2UserService() {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val email = oAuth2User.attributes["email"] as String?
            ?: throw OAuth2AuthenticationException(OAuth2Error("missing_email"), "Email não fornecido pelo provedor Google.")
        val nome = oAuth2User.attributes["name"] as String?
            ?: throw OAuth2AuthenticationException(OAuth2Error("missing_name"), "Nome não fornecido pelo provedor Google.")

        var usuario = usuarioRepository.findByEmail(email)

        if (usuario == null) {
            if (email.endsWith("@ifce.edu.br")) {
                usuario = Usuario(
                    email = email,
                    nome = nome
                )
                usuario.perfis.add(Perfil.ROLE_COLABORADOR)
                usuarioRepository.save(usuario)
            } else {
                throw OAuth2AuthenticationException(OAuth2Error("unauthorized_domain"), "Acesso Negado: Usuário externo não cadastrado.")
            }
        } else if (usuario.nome != nome) {
            usuario.nome = nome
            usuarioRepository.save(usuario)
        }

        if (!usuario.ativo) throw OAuth2AuthenticationException(OAuth2Error("user_inactive"), "Acesso Negado: Usuário inativo.")

        val authorities = usuario.perfis.map { SimpleGrantedAuthority(it.name) }

        return DefaultOAuth2User(authorities, oAuth2User.attributes, "email")
    }
}