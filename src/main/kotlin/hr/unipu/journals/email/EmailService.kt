package hr.unipu.journals.email

import jakarta.mail.internet.MimeMessage
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(private val mailSender: JavaMailSender) {
    fun send(
        to: String,
        subject: String,
        text: String
    ): Unit = mailSender.send(SimpleMailMessage().apply {
        setTo(to)
        setSubject(subject)
        setText(text)
    })
    fun sendHtml(
        to: String,
        subject: String,
        description: String
    ) {
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText("""
           <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <title>Email Template</title>
            </head>
            <body style="margin:0; padding:0; background-color:white; font-family: Arial, Helvetica, sans-serif;">
              <table width="100%" cellpadding="0" cellspacing="0" role="presentation">
                <tr>
                  <td align="center" style="padding:40px 20px;">
                    <table width="600" cellpadding="0" cellspacing="0" role="presentation" style="border:1px solid #000000;">
                      <tr>
                        <td style="padding:30px; text-align:center; color:black;">
                          <h1 style="margin:0 0 16px 0; font-size:24px; font-weight:bold;">Unipu Journals</h1>
                          <p style="margin:0; font-size:16px; line-height:1.5;">${description}</p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
        """.trimIndent(), true)
        mailSender.send(message)
    }
}