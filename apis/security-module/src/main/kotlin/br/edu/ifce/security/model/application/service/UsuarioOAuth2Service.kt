package br.edu.ifce.security.model.application.service

import br.edu.ifce.security.model.domain.Perfil
import br.edu.ifce.security.model.domain.Usuario
import br.edu.ifce.security.model.repository.UsuarioRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Sincroniza o [Usuario] a partir dos dados do provedor OAuth2.
 *
 * A transação cobre apenas o acesso ao banco: a chamada HTTP ao Google fica em
 * [CustomOAuth2UserService.loadUser], fora da transação. Assim o handshake OAuth2 não
 * segura conexão do pool e [CustomOAuth2UserService] deixa de ser proxiado por AOP.
 */
@Service
class UsuarioOAuth2Service(private val repository: UsuarioRepository) {

    @Transactional
    fun sincronizar(email: String, nome: String): List<SimpleGrantedAuthority> {
        var usuario = repository.findByEmail(email)

        if (usuario == null) {
            if (!email.endsWith(DOMINIO_INSTITUCIONAL))
                throw OAuth2AuthenticationException(
                    OAuth2Error("unauthorized_domain"),
                    "Acesso Negado: Usuário externo não cadastrado."
                )

            usuario = Usuario(email = email, nome = nome)
            usuario.perfis.add(Perfil.ROLE_COLABORADOR)
            repository.save(usuario)
        } else if (usuario.nome != nome) {
            usuario.nome = nome
            repository.save(usuario)
        }

        if (!usuario.ativo)
            throw OAuth2AuthenticationException(
                OAuth2Error("user_inactive"),
                "Acesso Negado: Usuário inativo."
            )

        return usuario.perfis.map { SimpleGrantedAuthority(it.name) }
    }

    companion object {
        private const val DOMINIO_INSTITUCIONAL = "@ifce.edu.br"
    }
}
