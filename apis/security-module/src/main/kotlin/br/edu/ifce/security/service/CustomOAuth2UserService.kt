package br.edu.ifce.security.service

import br.edu.ifce.security.domain.Perfil
import br.edu.ifce.security.domain.Usuario
import br.edu.ifce.security.repository.UsuarioRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class CustomOAuth2UserService(
    private val usuarioRepository: UsuarioRepository
) : DefaultOAuth2UserService() {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val email = oAuth2User.attributes["email"] as String?
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Email não fornecido pelo provedor OAuth2")
        val nome = oAuth2User.attributes["name"] as String?
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Nome não fornecido pelo provedor OAuth2")

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
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso Negado: Usuário externo não registrado.")
            }
        }

        if (!usuario.ativo) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso Negado: Usuário inativo.")

        val authorities = usuario.perfis.map { SimpleGrantedAuthority(it.name) }

        return DefaultOAuth2User(authorities, oAuth2User.attributes, "email")
    }
}