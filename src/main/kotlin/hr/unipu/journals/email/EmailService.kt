package hr.unipu.journals.email

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(private val mailSender: JavaMailSender) {
    fun sendSimpleMail(
        to: String,
        subject: String,
        text: String
    ): Unit = mailSender.send(SimpleMailMessage().apply {
        setTo(to)
        setSubject(subject)
        setText(text)
    })
}