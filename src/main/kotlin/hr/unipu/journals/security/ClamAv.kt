package hr.unipu.journals.security

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.file.Path
import java.util.UUID

enum class ScanResult { OK, FOUND }

@Service
class ClamAv {
    private val socketPath: Path = Path.of("/var/run/clamav/clamd.ctl")
    fun scanMultipartFile(multiPartFile: MultipartFile): ScanResult {
        val file = File.createTempFile(
            UUID.randomUUID().toString(),
            "-${multiPartFile.originalFilename}",
            File("/tmp")
        ).apply { deleteOnExit() }
        try {
            SocketChannel.open(UnixDomainSocketAddress.of(socketPath)).use { channel ->
                multiPartFile.transferTo(file)
                val command = "SCAN ${file.absolutePath}\n"
                channel.write(ByteBuffer.wrap(command.toByteArray()))
                val responseBuffer = ByteBuffer.allocate(1024)
                channel.read(responseBuffer)
                responseBuffer.flip()
                val response = String(responseBuffer.array(), 0, responseBuffer.limit()).trim()
                return when {
                    response.contains("OK") -> { println(response); return ScanResult.OK }
                    response.contains("FOUND") -> return ScanResult.FOUND
                    else -> ScanResult.FOUND
                }
            }
        } catch (_: Exception) { return ScanResult.FOUND }
        finally { file.delete() }
    }
}