package hr.unipu.journals.security

import hr.unipu.journals.util.AppProperties
import org.springframework.stereotype.Service
import java.io.File
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.file.Path

enum class ScanResult { OK, FOUND }

@Service
class ClamAv(appProperties: AppProperties) {
    private val socketPath: Path = Path.of(appProperties.clamavPath)
    val forbiddenExtensions = setOf(
        "exe", "msi", "bat", "cmd", "sh", "app", "apk", "dll", "so", "bin", "iso", "dmg", "img", "pkg",
        "html", "htm", "css", "js", "php", "asp", "aspx",
        "psd", "indd", "cdr", "sketch", "key",
        "gif", "webp", "heic", "heif", "raw",
        "hdf", "h5", "sav", "dta", "mat",
        "rar", "7z", "ace",
        "tmp", "bak", "~doc", "swp",
        "ttf", "otf", "fon", ""
    )
    fun scanMultipartFile(file: File): ScanResult {
        SocketChannel.open(UnixDomainSocketAddress.of(socketPath)).use { channel ->
            val command = "SCAN ${file.absolutePath}\n"
            channel.write(ByteBuffer.wrap(command.toByteArray()))
            val responseBuffer = ByteBuffer.allocate(1024)
            channel.read(responseBuffer)
            responseBuffer.flip()
            val response = String(responseBuffer.array(), 0, responseBuffer.limit()).trim()
            return when {
                response.contains("OK") -> { return ScanResult.OK }
                response.contains("FOUND") -> return ScanResult.FOUND
                else -> ScanResult.FOUND
            }
        }
    }
}