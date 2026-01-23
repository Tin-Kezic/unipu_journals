package hr.unipu.journals.feature.manuscript.file

import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
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
    fun file(
        @RequestParam id: Int,
        @RequestParam type: ManuscriptFileAccessType = ManuscriptFileAccessType.DOWNLOAD
    ): ResponseEntity<FileSystemResource> {
        val manuscriptFile = manuscriptFileRepository.byId(id) ?: throw IllegalArgumentException("failed to find file $id")
        val file = File(manuscriptFile.path)
        val mediaType = MediaTypeFactory.getMediaType(file.name).orElse(MediaType.APPLICATION_OCTET_STREAM)
        return ResponseEntity.ok()
            .contentType(mediaType)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "${if(type == ManuscriptFileAccessType.DOWNLOAD) "attachment" else "inline"}; filename=\"${manuscriptFile.name}\""
            )
            .contentLength(file.length())
            .body(FileSystemResource(file))
    }
}