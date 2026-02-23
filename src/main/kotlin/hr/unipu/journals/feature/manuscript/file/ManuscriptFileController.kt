package hr.unipu.journals.feature.manuscript.file

import hr.unipu.journals.feature.manuscript.review.file.ManuscriptReviewFileRepository
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
class ManuscriptFileController(
    private val manuscriptFileRepository: ManuscriptFileRepository,
    private val manuscriptReviewFileRepository: ManuscriptReviewFileRepository
) {
    @GetMapping
    fun file(
        @RequestParam id: Int,
        @RequestParam fileType: ManuscriptFileType = ManuscriptFileType.MANUSCRIPT,
        @RequestParam fileAccessType: ManuscriptFileAccessType = ManuscriptFileAccessType.DOWNLOAD,
    ): ResponseEntity<FileSystemResource> {
        val (name, path) = when(fileType) {
            ManuscriptFileType.MANUSCRIPT -> manuscriptFileRepository.byId(id)?.let { Pair(it.name, it.path) } ?: throw IllegalArgumentException("failed to find file $id")
            ManuscriptFileType.REVIEW -> manuscriptReviewFileRepository.byId(id)?.let { Pair(it.name, it.path) } ?: throw IllegalArgumentException("failed to find file $id")
        }
        val file = File(path)
        val mediaType = MediaTypeFactory.getMediaType(file.name).orElse(MediaType.APPLICATION_OCTET_STREAM)
        return ResponseEntity.ok()
            .contentType(mediaType)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "${if(fileAccessType == ManuscriptFileAccessType.DOWNLOAD) "attachment" else "inline"}; filename=\"${name}\""
            )
            .contentLength(file.length())
            .body(FileSystemResource(file))
    }
}