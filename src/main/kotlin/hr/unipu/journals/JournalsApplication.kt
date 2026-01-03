package hr.unipu.journals

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class JournalsApplication

fun main(args: Array<String>) {
	runApplication<JournalsApplication>(*args)
}