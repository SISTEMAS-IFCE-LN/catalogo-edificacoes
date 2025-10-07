package br.edu.ifce.ambientes_internos.model.service

import br.edu.ifce.ambientes_internos.model.domain.Hello
import org.springframework.stereotype.Service

@Service
class HelloService {

    fun getHelloMessage(): Hello {
        return Hello()
    }

}