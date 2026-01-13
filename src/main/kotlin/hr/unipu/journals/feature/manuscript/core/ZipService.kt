package hr.unipu.journals.feature.manuscript.core

import net.lingala.zip4j.ZipFile
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files

@Service
class ZipService {
    fun isEncrypted(file: File): Boolean {
        val zipFile = ZipFile(file)
        zipFile.fileHeaders.forEach { if(it.isEncrypted) return true }
        return false
    }
}