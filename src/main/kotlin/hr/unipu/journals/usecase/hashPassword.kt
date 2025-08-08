package hr.unipu.journals.usecase

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

fun hashPassword(password: String): String = BCryptPasswordEncoder().encode(password)
