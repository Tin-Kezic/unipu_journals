package hr.unipu.journals.feature.manuscript.core

import net.lingala.zip4j.ZipFile
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files

@Service
class ZipService {
    fun isEncrypted(file: MultipartFile): Boolean {
        val temp = Files.createTempFile("upload", ".zip").toFile().apply { deleteOnExit() }
        try {
            file.inputStream.use { it.copyTo(temp.outputStream()) }
            val zipFile = ZipFile(temp)
            zipFile.fileHeaders.forEach { if(it.isEncrypted) return true }
        } catch (_: Exception) { return true }
        finally { temp.delete() }
        return false
    }
}