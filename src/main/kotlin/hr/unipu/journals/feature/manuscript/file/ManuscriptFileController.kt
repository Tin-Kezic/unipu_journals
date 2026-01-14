package hr.unipu.journals.feature.manuscript.file

import hr.unipu.journals.util.Global
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File

@RestController
@RequestMapping("/files")
class ManuscriptFileController(private val manuscriptFileRepository: ManuscriptFileRepository) {
    @GetMapping
    fun file(@RequestParam id: Int): ResponseEntity<FileSystemResource> {
        val manuscriptFile = manuscriptFileRepository.byId(id) ?: throw IllegalArgumentException("failed to find file $id")
        val file = File(manuscriptFile.path)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"${file.name.drop(Global.UUID_LENGTH)}\""
            )
            .contentLength(file.length())
            .body(FileSystemResource(file))
    }
}