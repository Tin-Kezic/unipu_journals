package hr.unipu.journals

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@SpringBootApplication
class JournalsApplication

fun main(args: Array<String>) {
	runApplication<JournalsApplication>(*args)
}