package hr.unipu.journals.security

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
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
        val uuid = UUID.randomUUID()
        val file = File.createTempFile("$uuid", "-${multiPartFile.originalFilename}", File("/tmp")).apply { deleteOnExit() }
        SocketChannel.open(UnixDomainSocketAddress.of(socketPath)).use { channel ->
            multiPartFile.transferTo(file)
            val command = "SCAN ${file.absolutePath}\n"
            channel.write(ByteBuffer.wrap(command.toByteArray()))
            val responseBuffer = ByteBuffer.allocate(1024)
            channel.read(responseBuffer)
            responseBuffer.flip()
            val response = String(responseBuffer.array(), 0, responseBuffer.limit()).trim()
            when {
                response.contains("OK") -> { println(response); return ScanResult.OK }
                response.contains("FOUND") -> return ScanResult.FOUND
            }
        }
        return ScanResult.FOUND
    }
    fun ping(): String {
        SocketChannel.open(UnixDomainSocketAddress.of(socketPath)).use { channel ->
            channel.configureBlocking(true)

            fun writeFully(buf: ByteBuffer) {
                while (buf.hasRemaining()) {
                    val written = channel.write(buf)
                    if (written == 0) Thread.sleep(1)
                }
            }
            writeFully(ByteBuffer.wrap("PING\n".toByteArray(Charsets.US_ASCII)))

            val readBuf = ByteBuffer.allocate(1024)
            val baos = ByteArrayOutputStream()

            while (true) {
                val bytesRead = channel.read(readBuf)
                if (bytesRead == -1) break
                if (bytesRead == 0) continue
                readBuf.flip()
                val bytes = ByteArray(readBuf.remaining())
                readBuf.get(bytes)
                baos.write(bytes)
                readBuf.clear()
            }
            val response = baos.toString("UTF-8").trim()
            return response
        }
    }
}