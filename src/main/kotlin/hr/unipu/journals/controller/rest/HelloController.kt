package hr.unipu.journals.controller.rest

import hr.unipu.journals.data.getFakeData
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {
    @GetMapping("/")
    fun test(): String {
        return getFakeData()
    }
}