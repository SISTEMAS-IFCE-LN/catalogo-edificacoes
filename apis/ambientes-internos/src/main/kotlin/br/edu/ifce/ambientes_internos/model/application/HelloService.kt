package br.edu.ifce.ambientes_internos.model.application

import br.edu.ifce.ambientes_internos.model.Hello
import org.springframework.stereotype.Service

@Service
class HelloService {

    fun getHelloMessage(): Hello {
        return Hello()
    }

}