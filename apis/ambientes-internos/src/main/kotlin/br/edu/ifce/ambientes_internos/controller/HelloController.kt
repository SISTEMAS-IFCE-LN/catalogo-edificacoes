package br.edu.ifce.ambientes_internos.controller

import br.edu.ifce.ambientes_internos.model.Hello
import br.edu.ifce.ambientes_internos.model.service.HelloService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/hello")
class HelloController(val helloService: HelloService) {

    @GetMapping
    fun getHelloMessage(): Hello {
        return helloService.getHelloMessage()
    }

}