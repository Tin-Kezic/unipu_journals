package hr.unipu.journals

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JournalsApplication

fun main(args: Array<String>) {
	runApplication<JournalsApplication>(*args)
}
