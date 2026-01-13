package hr.unipu.journals.feature.manuscript.core

import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.invite.InvitationTarget
import hr.unipu.journals.feature.invite.InviteRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.ManuscriptRole
import hr.unipu.journals.feature.manuscript.category.CategoryRepository
import hr.unipu.journals.feature.manuscript.file.ManuscriptFileRepository
import hr.unipu.journals.feature.publication.core.PublicationRepository
import hr.unipu.journals.feature.publication.core.Role
import hr.unipu.journals.feature.publication.core.Sorting
import hr.unipu.journals.feature.section.core.SectionRepository
import hr.unipu.journals.feature.unregistered_author.UnregisteredAuthorRepository
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_AUTHENTICATED
import hr.unipu.journals.security.AuthorizationService
import hr.unipu.journals.security.ClamAv
import hr.unipu.journals.security.ScanResult
import hr.unipu.journals.util.AppProperties
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.UUID

@RestController
@RequestMapping("/api/manuscripts")
class ManuscriptController(
    private val publicationRepository: PublicationRepository,
    private val sectionRepository: SectionRepository,
    private val manuscriptRepository: ManuscriptRepository,
    private val manuscriptFileRepository: ManuscriptFileRepository,
    private val accountRepository: AccountRepository,
    private val inviteRepository: InviteRepository,
    private val authorizationService: AuthorizationService,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val unregisteredAuthorRepository: UnregisteredAuthorRepository,
    private val categoryRepository: CategoryRepository,
    private val zipService: ZipService,
    private val clamAv: ClamAv,
    private val appProperties: AppProperties
) {
    private fun toAwaitingManuscript() {

    }
    @GetMapping
    fun all(
        @RequestParam publicationId: Int?,
        @RequestParam sectionId: Int?,
        @RequestParam manuscriptStateFilter: ManuscriptStateFilter,
        @RequestParam role: Role?,
        @RequestParam category: String?,
        @RequestParam sorting: Sorting = Sorting.ALPHABETICAL_A_Z,
        @RequestParam from: String?,
        @RequestParam to: String?,
        @RequestParam accountId: Int?,
        @RequestParam query: String?
    ): List<Map<String, Any?>> {
        require(
            manuscriptStateFilter in setOf(ManuscriptStateFilter.PUBLISHED, ManuscriptStateFilter.ARCHIVED)
                    || accountId == null
                    || with(authorizationService) { this.isAccountOwner(accountId) || this.isAdmin }
        )
        val account = accountId?.let { accountRepository.byId(it) } ?: authorizationService.account
        val manuscriptsAndAuthors = manuscriptRepository.all(
            accountId = account?.id,
            manuscriptStateFilter = manuscriptStateFilter,
            role = role,
            sectionId = sectionId,
            category = category,
            sorting = sorting,
            from = from,
            to = to,
            query = query
        ).map { manuscript -> Triple(
            manuscript,
            accountRoleOnManuscriptRepository.authors(manuscript.id),
            unregisteredAuthorRepository.authors(manuscript.id)
        )}
        if(manuscriptStateFilter.name.contains("AWAITING")) {
            require(account != null)
            val pending = manuscriptsAndAuthors.map { (manuscript, registeredAuthors, unregisteredAuthors) -> buildMap {
                put("type", "pending")
                put("roles", manuscript.roles)
                put("id", manuscript.id)
                put("title", manuscript.title)
                put("registeredAuthors", registeredAuthors.map { author -> mapOf("id" to author.id, "fullName" to author.fullName) })
                put("unregisteredAuthors",
                    if(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscript.id)
                                || authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)
                                || authorizationService.isAdmin)
                        unregisteredAuthors else unregisteredAuthors.map { author -> author.fullName }
                )
                put("correspondingAuthor",
                    accountRepository.byEmail(manuscript.correspondingAuthorEmail)?.let { mapOf("type" to "registered", "id" to it.id, "fullName" to it.fullName) }
                        ?: unregisteredAuthorRepository.byEmail(manuscript.correspondingAuthorEmail)?.let { unregisteredAuthor ->
                            if(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscript.id)
                                || authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)
                                || authorizationService.isAdmin)
                                unregisteredAuthor.let { author -> mapOf(
                                    "type" to "unregistered",
                                    "id" to author.id,
                                    "fullName" to author.fullName,
                                    "email" to author.email,
                                    "country" to author.country,
                                    "affiliation" to author.affiliation,
                                    "manuscriptId" to author.manuscriptId
                                )}
                            else unregisteredAuthor.fullName
                        }
                )
                put("files", manuscriptFileRepository.allFilesByManuscriptId(manuscript.id).map { mapOf("id" to it.id, "name" to it.name) })
                put("submissionDate", manuscript.submissionDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                put("publicationDate", manuscript.publicationDate?.format(DateTimeFormatter.ISO_LOCAL_DATE))
                put("description", manuscript.description)
                put("state", manuscript.state)
            }}
            val invited = inviteRepository.invitedManuscripts(
                email = account.email,
                manuscriptStateFilter = manuscriptStateFilter,
                role = role,
                accountId = account.id,
                sectionId = sectionId,
                category = category,
                sorting = sorting
            ).map { manuscript -> Triple(
                manuscript,
                accountRoleOnManuscriptRepository.authors(manuscript.id),
                unregisteredAuthorRepository.authors(manuscript.id)
            )}.map { (manuscript, registeredAuthors, unregisteredAuthors) -> buildMap {
                put("type", "invited")
                put("role", manuscript.role)
                put("id", manuscript.id)
                put("title", manuscript.title)
                put("registeredAuthors", registeredAuthors.map { author -> mapOf("id" to author.id, "fullName" to author.fullName) })
                put("unregisteredAuthors",
                    if(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscript.id)
                        || authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)
                        || authorizationService.isAdmin
                        )
                        unregisteredAuthors else unregisteredAuthors.map { author -> author.fullName }
                )
                put("correspondingAuthor",
                    accountRepository.byEmail(manuscript.correspondingAuthorEmail)?.let { mapOf("type" to "registered", "id" to it.id, "fullName" to it.fullName) }
                        ?: unregisteredAuthorRepository.byEmail(manuscript.correspondingAuthorEmail)?.let { unregisteredAuthor ->
                            if(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscript.id)
                                || authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)
                                || authorizationService.isAdmin
                                )
                                unregisteredAuthor.let { author -> mapOf(
                                    "type" to "unregistered",
                                    "id" to author.id,
                                    "fullName" to author.fullName,
                                    "email" to author.email,
                                    "country" to author.country,
                                    "affiliation" to author.affiliation,
                                    "manuscriptId" to author.manuscriptId
                                )}
                            else unregisteredAuthor.fullName
                        }
                )
                put("files", manuscriptFileRepository.allFilesByManuscriptId(manuscript.id).map { mapOf("id" to it.id, "name" to it.name) })
                put("submissionDate", manuscript.submissionDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                put("publicationDate", manuscript.publicationDate?.format(DateTimeFormatter.ISO_LOCAL_DATE))
                put("description", manuscript.description)
                put("state", manuscript.state)
            }}
            return pending + invited
        }
        return manuscriptsAndAuthors.map { (manuscript, registeredAuthors, unregisteredAuthors) -> buildMap {
            put("roles", manuscript.roles)
            put("id", manuscript.id)
            put("title", manuscript.title)
            put("registeredAuthors", registeredAuthors.map { author -> mapOf("id" to author.id, "fullName" to author.fullName) })
            put("unregisteredAuthors",
                if(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscript.id)
                    || authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)
                    || authorizationService.isAdmin
                    )
                    unregisteredAuthors else unregisteredAuthors.map { author -> author.fullName }
            )
            put("correspondingAuthor",
                accountRepository.byEmail(manuscript.correspondingAuthorEmail)?.let { mapOf("type" to "registered", "id" to it.id, "fullName" to it.fullName) }
                    ?: unregisteredAuthorRepository.byEmail(manuscript.correspondingAuthorEmail)?.let { unregisteredAuthor ->
                        if(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscript.id)
                            || authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)
                            || authorizationService.isAdmin)
                            unregisteredAuthor.let { author -> mapOf(
                                "type" to "unregistered",
                                "id" to author.id,
                                "fullName" to author.fullName,
                                "email" to author.email,
                                "country" to author.country,
                                "affiliation" to author.affiliation,
                                "manuscriptId" to author.manuscriptId
                            )}
                        else unregisteredAuthor.fullName
                    }
            )
            put("files", manuscriptFileRepository.allFilesByManuscriptId(manuscript.id).map { mapOf("id" to it.id, "name" to it.name) })
            put("submissionDate", manuscript.submissionDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
            put("publicationDate", manuscript.publicationDate?.format(DateTimeFormatter.ISO_LOCAL_DATE))
            put("isOverseeing", (authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)
                    || authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscript.id))
                    || authorizationService.isAdmin
            )
            put("description", manuscript.description)
            put("state", manuscript.state)
        }}
    }
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_AUTHENTICATED)
    fun insert(
        @RequestPart manuscriptSubmission: ManuscriptSubmission,
        @RequestPart files: List<MultipartFile>,
    ): ResponseEntity<String> {
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
        files.forEach { file ->
            if(file.originalFilename == null)
                return ResponseEntity.badRequest().body("submitted unnamed files")
        }
        val tempFiles = files.map { file -> File.createTempFile(
            UUID.randomUUID().toString(),
            "-${Jsoup.clean(file.originalFilename!!, Safelist.none()).replace("-", "_")}",
            File("/tmp")).apply { deleteOnExit() }
        }
        files.zip(tempFiles).forEach { (file, temp) -> file.transferTo(temp) }
        try {
            tempFiles.forEach { file ->
                val extension = file.name.substringAfterLast('.', "").lowercase()
                if(extension in forbiddenExtensions)
                    return ResponseEntity.badRequest().body("files of type .$extension are not allowed.")
                if(extension == "zip" && zipService.isEncrypted(file))
                    return ResponseEntity.badRequest().body("submitted zip files are encrypted, corrupted or malformed")
                if(clamAv.scanMultipartFile(file) == ScanResult.FOUND)
                    return ResponseEntity.badRequest().body("submitted files contain malware")
            }
            val insertedManuscript = manuscriptRepository.insert(
                title = Jsoup.clean(manuscriptSubmission.title, Safelist.none()),
                description = Jsoup.clean(manuscriptSubmission.description, Safelist.none()),
                categoryId = categoryRepository.idByName(Jsoup.clean(manuscriptSubmission.category, Safelist.none())),
                sectionId = sectionRepository.idByName(
                    publicationTitle = Jsoup.clean(manuscriptSubmission.publicationTitle, Safelist.none()),
                    sectionTitle = Jsoup.clean(manuscriptSubmission.sectionName, Safelist.none())
                ),
                correspondingAuthorEmail = Jsoup.clean(manuscriptSubmission.correspondingAuthorEmail, Safelist.none())
            )
            tempFiles.forEach { file ->
                val path = "${appProperties.fileStoragePath}/unipu-journals/files/${file.name}"
                file.copyTo(File(path), true)
                manuscriptFileRepository.insert(
                    name = file.name.substringAfterLast("-"),
                    path = path,
                    manuscriptId = insertedManuscript.id
                )
            }
            manuscriptSubmission.authors.forEach { authorDTO ->
                val account = accountRepository.byEmail(authorDTO.email)
                if(account != null) accountRoleOnManuscriptRepository.assign(ManuscriptRole.AUTHOR, account.id, insertedManuscript.id)
                else unregisteredAuthorRepository.insert(
                    fullName = authorDTO.fullName,
                    email = authorDTO.email,
                    country = authorDTO.country,
                    affiliation = authorDTO.affiliation,
                    manuscriptId = insertedManuscript.id
                )
            }
            accountRoleOnManuscriptRepository.allEicOnPublicationEmailsByPublicationTitle(manuscriptSubmission.publicationTitle).forEach { eicEmail ->
                inviteRepository.invite(eicEmail, InvitationTarget.EIC_ON_MANUSCRIPT, insertedManuscript.id)
            }
            return ResponseEntity.ok("manuscript successfully added")
        } finally {
            tempFiles.forEach { file -> file.delete() }
        }
    }
    @PutMapping("/{manuscriptId}")
    fun updateState(
        @PathVariable manuscriptId: Int,
        @RequestParam newState: ManuscriptState
    ): ResponseEntity<String> {
        val manuscript = manuscriptRepository.byId(manuscriptId) ?: return ResponseEntity.badRequest().body("failed to find manuscript $manuscriptId")
        val section = sectionRepository.byId(manuscript.sectionId)!!
        val publication = publicationRepository.byId(section.publicationId)!!
        val isAdmin = authorizationService.isAdmin
        val isSectionEditorOnSectionOrSuperior = authorizationService.isSectionEditorOnSectionOrSuperior(publication.id, section.id)
        val isEditorOnManuscriptOrAffiliatedSuperior = authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscriptId)
        when(newState) {
            ManuscriptState.ARCHIVED -> {
                if(isSectionEditorOnSectionOrSuperior.not()) return ResponseEntity.status(403).body("forbidden to archive manuscripts in section ${section.id}")
                if(manuscript.state != ManuscriptState.PUBLISHED) return ResponseEntity.badRequest().body("cannot archive manuscript that is not published")
            }
            ManuscriptState.HIDDEN -> {
                if(isAdmin.not()) return ResponseEntity.status(403).body("forbidden to hide manuscripts")
                if(manuscript.state !in setOf(ManuscriptState.PUBLISHED, ManuscriptState.REJECTED)) return ResponseEntity.badRequest().body("cannot hide manuscript that is not published or rejected")
            }
            ManuscriptState.AWAITING_EIC_REVIEW -> return ResponseEntity.badRequest().body("cannot change manuscript state to $newState")
            ManuscriptState.AWAITING_EDITOR_REVIEW -> {
                if(authorizationService.isEicOnManuscript(manuscriptId).not()) return ResponseEntity.status(403).body("forbidden to EiC review manuscripts $manuscriptId")
                if(manuscript.state != ManuscriptState.AWAITING_EIC_REVIEW) return ResponseEntity.badRequest().body("cannot change state to AWAITING_EDITOR_REVIEW from $newState")
            }
            ManuscriptState.AWAITING_REVIEWER_REVIEW -> {
                if(isEditorOnManuscriptOrAffiliatedSuperior.not()) return ResponseEntity.status(403).body("forbidden to initialize round on manuscript $manuscriptId")
                if(manuscript.state != ManuscriptState.AWAITING_EDITOR_REVIEW) return ResponseEntity.badRequest().body("cannot initialize round from $newState")
            }
            ManuscriptState.MINOR, ManuscriptState.MAJOR, ManuscriptState.REJECTED -> {
                if(isEditorOnManuscriptOrAffiliatedSuperior.not()) return ResponseEntity.status(403).body("forbidden to determine minor on manuscript $manuscriptId")
            }
            ManuscriptState.PUBLISHED -> when(manuscript.state) {
                ManuscriptState.HIDDEN -> if(isAdmin.not()) return ResponseEntity.status(403).body("forbidden to unhide manuscripts")
                ManuscriptState.ARCHIVED -> if(isSectionEditorOnSectionOrSuperior.not()) return ResponseEntity.status(403).body("forbidden to unarchive manuscripts in section ${section.id}")
                ManuscriptState.AWAITING_REVIEWER_REVIEW -> {
                    if(isEditorOnManuscriptOrAffiliatedSuperior.not()) return ResponseEntity.status(403).body("forbidden to determine minor on manuscript $manuscriptId")
                }
                else -> return ResponseEntity.badRequest().body("cannot change state to PUBLISHED from $newState")
            }
        }
        val rowsAffected = manuscriptRepository.updateState(manuscriptId, newState)
        return if(rowsAffected == 1) ResponseEntity.ok("successfully updated state on manuscript: $manuscriptId")
        else ResponseEntity.internalServerError().body("failed to update state on manuscript: $manuscriptId")
    }
}