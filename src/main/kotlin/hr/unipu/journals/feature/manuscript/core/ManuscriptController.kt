package hr.unipu.journals.feature.manuscript.core

import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.invite.InviteRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.publication.core.Role
import hr.unipu.journals.feature.publication.core.Sorting
import hr.unipu.journals.feature.unregistered_author.UnregisteredAuthor
import hr.unipu.journals.feature.unregistered_author.UnregisteredAuthorRepository
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_AUTHENTICATED
import hr.unipu.journals.security.AuthorizationService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/manuscripts")
class ManuscriptController(
    private val manuscriptRepository: ManuscriptRepository,
    private val accountRepository: AccountRepository,
    private val inviteRepository: InviteRepository,
    private val authorizationService: AuthorizationService,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val unregisteredAuthorRepository: UnregisteredAuthorRepository
) {
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
        @RequestParam accountId: Int?
    ): List<Map<String, Any?>> {
        require(
            manuscriptStateFilter in listOf(ManuscriptStateFilter.PUBLISHED, ManuscriptStateFilter.ARCHIVED)
                    || accountId == null
                    || with(authorizationService) { this.isAccountOwner(accountId) || this.isAdmin }
        )
        val account = accountId?.let {accountRepository.byId(it) } ?: authorizationService.account
        val manuscriptsAndAuthors = manuscriptRepository.all(
            accountId = account?.id,
            manuscriptStateFilter = manuscriptStateFilter,
            role = role,
            sectionId = sectionId,
            category = category,
            sorting = sorting,
            from = from,
            to = to
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
                put("downloadUrl", manuscript.downloadUrl)
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
                put("downloadUrl", manuscript.downloadUrl)
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
            put("downloadUrl", manuscript.downloadUrl)
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
    @PostMapping
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_AUTHENTICATED)
    fun insert(
        @PathVariable sectionId: Int,
        @RequestParam title: String,
        @RequestParam category: String,
        @RequestParam authors: List<UnregisteredAuthor>,
        @RequestParam abstract: String,
        @RequestParam files: List<MultipartFile>,
    ): ResponseEntity<String> {
        // clean inputs with Jsoup
        /*
        publicationRepository.insert(
            title = Jsoup.clean(title, Safelist.none()),
            category = Jsoup.clean(category, Safelist.none()),
            authors = authors,
            abstract = abstract,
            files = files
        )
         */
        return ResponseEntity.ok("manuscript successfully added")
    }
    @PutMapping("/{manuscriptId}")
    fun updateState(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        @PathVariable manuscriptId: Int,
        @RequestParam newState: ManuscriptState
    ): ResponseEntity<String> {
        val manuscript = manuscriptRepository.byId(manuscriptId) ?: return ResponseEntity.badRequest().body("failed to find manuscript $manuscriptId")
        val isAdmin = authorizationService.isAdmin
        val isSectionEditorOnSectionOrSuperior = authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)
        val isEditorOnManuscriptOrAffiliatedSuperior = authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscriptId)
        when(newState) {
            ManuscriptState.ARCHIVED -> {
                if(isSectionEditorOnSectionOrSuperior.not()) return ResponseEntity.status(403).body("unauthorized to archive manuscripts in section $sectionId")
                if(manuscript.state != ManuscriptState.PUBLISHED) return ResponseEntity.badRequest().body("cannot archive manuscript that is not published")
            }
            ManuscriptState.HIDDEN -> {
                if(isAdmin.not()) return ResponseEntity.status(403).body("unauthorized to hide manuscripts")
                if(manuscript.state !in listOf(ManuscriptState.PUBLISHED, ManuscriptState.REJECTED)) return ResponseEntity.badRequest().body("cannot hide manuscript that is not published or rejected")
            }
            ManuscriptState.AWAITING_EIC_REVIEW -> return ResponseEntity.badRequest().body("cannot change manuscript state to $newState")
            ManuscriptState.AWAITING_EDITOR_REVIEW -> {
                if(authorizationService.isEicOnManuscript(manuscriptId).not()) return ResponseEntity.status(403).body("unauthorized to EiC review manuscripts $manuscriptId")
                if(manuscript.state != ManuscriptState.AWAITING_EIC_REVIEW) return ResponseEntity.badRequest().body("cannot change state to AWAITING_EDITOR_REVIEW from $newState")
            }
            ManuscriptState.AWAITING_REVIEWER_REVIEW -> {
                if(isEditorOnManuscriptOrAffiliatedSuperior.not()) return ResponseEntity.status(403).body("unauthorized to initialize round on manuscript $manuscriptId")
                if(manuscript.state != ManuscriptState.AWAITING_EDITOR_REVIEW) return ResponseEntity.badRequest().body("cannot initialize round from $newState")
            }
            ManuscriptState.MINOR, ManuscriptState.MAJOR, ManuscriptState.REJECTED -> {
                if(isEditorOnManuscriptOrAffiliatedSuperior.not()) return ResponseEntity.status(403).body("unauthorized to determine minor on manuscript $manuscriptId")
            }
            ManuscriptState.PUBLISHED -> when(manuscript.state) {
                ManuscriptState.HIDDEN -> if(isAdmin.not()) return ResponseEntity.status(403).body("unauthorized to unhide manuscripts")
                ManuscriptState.ARCHIVED -> if(isSectionEditorOnSectionOrSuperior.not()) return ResponseEntity.status(403).body("unauthorized to unarchive manuscripts in section $sectionId")
                ManuscriptState.AWAITING_REVIEWER_REVIEW -> {
                    if(isEditorOnManuscriptOrAffiliatedSuperior.not()) return ResponseEntity.status(403).body("unauthorized to determine minor on manuscript $manuscriptId")
                }
                else -> return ResponseEntity.badRequest().body("cannot change state to PUBLISHED from $newState")
            }
        }
        val rowsAffected = manuscriptRepository.updateState(manuscriptId, newState)
        return if(rowsAffected == 1) ResponseEntity.ok("successfully updated state on manuscript: $manuscriptId")
        else ResponseEntity.internalServerError().body("failed to update state on manuscript: $manuscriptId")
    }
}