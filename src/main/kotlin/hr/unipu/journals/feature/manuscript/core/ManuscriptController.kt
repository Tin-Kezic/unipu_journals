package hr.unipu.journals.feature.manuscript.core

import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.invite.InvitationTarget
import hr.unipu.journals.feature.invite.InviteRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.ManuscriptRole
import hr.unipu.journals.feature.manuscript.category.CategoryRepository
import hr.unipu.journals.feature.manuscript.file.ManuscriptFileRepository
import hr.unipu.journals.feature.manuscript.file.ManuscriptFileService
import hr.unipu.journals.feature.publication.core.PublicationRepository
import hr.unipu.journals.feature.publication.core.Role
import hr.unipu.journals.feature.publication.core.Sorting
import hr.unipu.journals.feature.publication.eic_on_publication.EicOnPublicationRepository
import hr.unipu.journals.feature.section.core.SectionRepository
import hr.unipu.journals.feature.unregistered_author.UnregisteredAuthorRepository
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_AUTHENTICATED
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_EIC_ON_MANUSCRIPT_OR_SUPERIOR
import hr.unipu.journals.security.AuthorizationService
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
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
import java.time.LocalDate

@RestController
@RequestMapping("/api/manuscripts")
class ManuscriptController(
    private val publicationRepository: PublicationRepository,
    private val sectionRepository: SectionRepository,
    private val manuscriptService: ManuscriptService,
    private val manuscriptRepository: ManuscriptRepository,
    private val manuscriptFileService: ManuscriptFileService,
    private val manuscriptFileRepository: ManuscriptFileRepository,
    private val accountRepository: AccountRepository,
    private val inviteRepository: InviteRepository,
    private val authorizationService: AuthorizationService,
    private val eicOnPublicationRepository: EicOnPublicationRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val unregisteredAuthorRepository: UnregisteredAuthorRepository,
    private val categoryRepository: CategoryRepository,
) {
    @GetMapping
    fun all(
        @RequestParam publicationId: Int?,
        @RequestParam sectionId: Int?,
        @RequestParam manuscriptStateFilter: ManuscriptStateFilter,
        @RequestParam role: Role?,
        @RequestParam category: String?,
        @RequestParam sorting: Sorting = Sorting.ALPHABETICAL_A_Z,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
        @RequestParam accountId: Int?,
        @RequestParam query: String?
    ): List<Map<String, Any?>> {
        val account = accountId?.let { accountRepository.byId(it) } ?: authorizationService.account
        require(
            manuscriptStateFilter in setOf(ManuscriptStateFilter.PUBLISHED, ManuscriptStateFilter.ARCHIVED)
                    || account?.id != null
                    || authorizationService.isAdmin
        )
        val manuscripts = manuscriptRepository.all(
            accountId = account?.id,
            manuscriptStateFilter = manuscriptStateFilter,
            role = role,
            sectionId = sectionId,
            category = category,
            sorting = sorting,
            from = from,
            to = to,
            query = query
        ).map { manuscriptService.toManuscriptDto(manuscript = it) }
        if(manuscriptStateFilter.name.contains("AWAITING")) {
            require(account != null)
            val pending = manuscripts.map { manuscript -> manuscript + ("type" to "pending")}
            val invites = inviteRepository.invitedManuscripts(
                email = account.email,
                manuscriptStateFilter = manuscriptStateFilter,
                role = role,
                accountId = account.id,
                sectionId = sectionId,
                category = category,
                sorting = sorting
            )
            val invited = invites.map {
                manuscriptRepository.byId(it.targetId)!!
            }.map { manuscriptService.toManuscriptDto(it) }.zip(invites).map { (map, invite) -> (map - "roles") + mapOf(
                "type" to "invited",
                "role" to invite.target
            )}
            return pending + invited
        }
        return manuscripts
    }
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_AUTHENTICATED)
    fun insert(
        @RequestPart manuscriptSubmission: ManuscriptSubmission,
        @RequestPart files: List<MultipartFile>,
    ): ResponseEntity<String> {
        if(manuscriptSubmission.authors.size != manuscriptSubmission.authors.distinctBy { it.email }.size)
            return ResponseEntity.badRequest().body("duplicate author email")
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
        val response = manuscriptFileService.insert(files = files, manuscriptId = insertedManuscript.id)
        if(response.statusCode != HttpStatus.OK) return response
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
        eicOnPublicationRepository.eicEmailsByPublicationId(
            publicationRepository.by(title = manuscriptSubmission.publicationTitle)?.id
                ?: return ResponseEntity.internalServerError().body("failed to find publication")
        ).forEach { eicEmail ->
            inviteRepository.invite(eicEmail, InvitationTarget.EIC_ON_MANUSCRIPT, insertedManuscript.id)
        }
        return ResponseEntity.ok("manuscript successfully added")
    }
    @PutMapping("/{manuscriptId}")
    fun updateState(
        @PathVariable manuscriptId: Int,
        @RequestParam newState: ManuscriptState
    ): ResponseEntity<String> = manuscriptService.updateState(manuscriptId = manuscriptId, newState = newState)

    @PutMapping("/{manuscriptId}/technical-processing")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_EIC_ON_MANUSCRIPT_OR_SUPERIOR)
    fun technicalProcessing(
        @PathVariable manuscriptId: Int,
        @RequestPart registeredAuthorIds: List<Int>,
        @RequestPart manuscriptSubmission: ManuscriptTechnicalProcessingSubmission,
        @RequestPart correspondingAuthor: String, // if(registered) id else email
        @RequestPart files: List<MultipartFile>?,
    ): ResponseEntity<String> {
        if(manuscriptRepository.byId(manuscriptId) == null) return ResponseEntity.badRequest().body("failed to find manuscript")
        if(manuscriptSubmission.authors.distinctBy { it.email }.size != manuscriptSubmission.authors.size) return ResponseEntity.badRequest().body("duplicate author email")
        val correspondingAuthorId = correspondingAuthor.toIntOrNull()
        val email = (if(correspondingAuthorId != null) accountRepository.byId(correspondingAuthorId)?.email else correspondingAuthor)
            ?: return ResponseEntity.internalServerError().body("failed to find corresponding author account")
        manuscriptRepository.update(
            id = manuscriptId,
            title = manuscriptSubmission.title,
            description = manuscriptSubmission.description,
            categoryId = categoryRepository.idByName(manuscriptSubmission.category),
            sectionId = sectionRepository.idByName(publicationTitle = manuscriptSubmission.publicationTitle, sectionTitle = manuscriptSubmission.sectionName),
            correspondingAuthorEmail = email
        )
        val oldRegisteredAuthorsIds = accountRoleOnManuscriptRepository.all(
            manuscriptId = manuscriptId,
            role = ManuscriptRole.AUTHOR
        ).map { it.accountId }.toSet()
        val newRegisteredAuthorsIds = registeredAuthorIds.toSet()
        val toInsert = newRegisteredAuthorsIds - oldRegisteredAuthorsIds
        val toRemove = oldRegisteredAuthorsIds - newRegisteredAuthorsIds
        toInsert.forEach { authorId -> accountRoleOnManuscriptRepository.assign(manuscriptId = manuscriptId, accountId = authorId, accountRole = ManuscriptRole.AUTHOR) }
        toRemove.forEach { authorId -> accountRoleOnManuscriptRepository.revoke(manuscriptId = manuscriptId, accountId = authorId, accountRole = ManuscriptRole.AUTHOR) }
        unregisteredAuthorRepository.delete(manuscriptId = manuscriptId)
        manuscriptSubmission.authors.forEach { authorDTO ->
            accountRepository.byEmail(authorDTO.email)?.let { account ->
                accountRoleOnManuscriptRepository.assign(manuscriptId = manuscriptId, accountId = account.id, accountRole = ManuscriptRole.AUTHOR)
            } ?: unregisteredAuthorRepository.insert(
                fullName = authorDTO.fullName,
                email = authorDTO.email,
                country = authorDTO.country,
                affiliation = authorDTO.affiliation,
                manuscriptId = manuscriptId
            )
        }
        if(files != null) {
            manuscriptFileRepository.delete(manuscriptId = manuscriptId)
            val response = manuscriptFileService.insert(files = files, manuscriptId = manuscriptId)
            if(response.statusCode != HttpStatus.OK) return response
        }
        return ResponseEntity.ok("successfully completed technical processing")
    }
}