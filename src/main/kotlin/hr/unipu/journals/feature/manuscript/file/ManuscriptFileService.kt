package hr.unipu.journals.feature.manuscript.file

import hr.unipu.journals.feature.manuscript.core.ZipService
import hr.unipu.journals.security.ClamAv
import hr.unipu.journals.security.ScanResult
import hr.unipu.journals.util.AppProperties
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.UUID

@Service
class ManuscriptFileService(
    private val manuscriptFileRepository: ManuscriptFileRepository,
    private val zipService: ZipService,
    private val clamAv: ClamAv,
    private val appProperties: AppProperties
) {
    fun insert(files: List<MultipartFile>, manuscriptId: Int): ResponseEntity<String> {
        files.forEach { file ->
            if(file.originalFilename == null)
                return ResponseEntity.badRequest().body("submitted unnamed files")
        }
        val tempFiles = files.map { file ->
            val cleanFileName = Jsoup.clean(file.originalFilename!!, Safelist.none())
            cleanFileName to File.createTempFile(
                UUID.randomUUID().toString(),
                "." + cleanFileName.substringAfter(".")
            ).apply { deleteOnExit() }
        }
        files.zip(tempFiles).forEach { (file, temp) -> file.transferTo(temp.second) }
        try {
            tempFiles.forEach { (name, file) ->
                val extension = file.name.substringAfterLast('.', "").lowercase()
                if(extension in clamAv.forbiddenExtensions)
                    return ResponseEntity.badRequest().body("files of type .$extension are not allowed")
                if(extension == "zip" && zipService.isEncrypted(file))
                    return ResponseEntity.badRequest().body("submitted zip files are encrypted, corrupted or malformed")
                if(clamAv.scanMultipartFile(file) == ScanResult.FOUND)
                    return ResponseEntity.badRequest().body("submitted files contain malware")
            }
            tempFiles.forEach { (name, file) ->
                val path = "${appProperties.fileStoragePath}/${file.name}"
                file.copyTo(File(path), true)
                manuscriptFileRepository.insert(name = name, path = path, manuscriptId = manuscriptId)
            }
            return ResponseEntity.ok("manuscript successfully added")
        } finally { tempFiles.forEach { (name, file) -> file.delete() } }
    }
}