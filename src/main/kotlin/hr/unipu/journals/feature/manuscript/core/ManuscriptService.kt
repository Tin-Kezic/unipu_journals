package hr.unipu.journals.feature.manuscript.core

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import hr.unipu.journals.EmailService
import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.ManuscriptRole
import hr.unipu.journals.feature.manuscript.category.CategoryRepository
import hr.unipu.journals.feature.manuscript.file.ManuscriptFileRepository
import hr.unipu.journals.feature.publication.core.PublicationRepository
import hr.unipu.journals.feature.section.core.SectionRepository
import hr.unipu.journals.feature.unregistered_author.UnregisteredAuthorRepository
import hr.unipu.journals.security.AuthorizationService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class ManuscriptService(
    private val manuscriptRepository: ManuscriptRepository,
    private val accountRepository: AccountRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val unregisteredAuthorRepository: UnregisteredAuthorRepository,
    private val manuscriptFileRepository: ManuscriptFileRepository,
    private val sectionRepository: SectionRepository,
    private val publicationRepository: PublicationRepository,
    private val categoryRepository: CategoryRepository,
    private val authorizationService: AuthorizationService,
    private val emailService: EmailService
) {
    fun toManuscriptDto(manuscript: Manuscript) = Triple(
        manuscript,
        accountRoleOnManuscriptRepository.all(role = ManuscriptRole.AUTHOR, manuscriptId = manuscript.id).map { accountRepository.byId(it.accountId)!! },
        unregisteredAuthorRepository.authors(manuscript.id)
    ).let { (manuscript, registeredAuthors, unregisteredAuthors) ->
        val section = sectionRepository.byId(manuscript.sectionId)!!
        val publication = publicationRepository.by(id = section.publicationId)!!
        return@let buildMap {
            put("id", manuscript.id)
            put("title", manuscript.title)
            authorizationService.account?.id?.let { id ->
                put("roles", jacksonObjectMapper().writeValueAsString(
                    accountRoleOnManuscriptRepository.all(manuscriptId = manuscript.id, accountId = id).map { it.accountRole }
                ))
            }
            put("registeredAuthors", jacksonObjectMapper().writeValueAsString(registeredAuthors.map { author -> mapOf("id" to author.id, "fullName" to author.fullName) }))
            put("unregisteredAuthors", jacksonObjectMapper().writeValueAsString(
                if(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscript.id)
                    || authorizationService.isSectionEditorOnSectionOrSuperior(publication.id, section.id)
                    || authorizationService.isAdmin)
                    unregisteredAuthors else unregisteredAuthors.map { author -> author.fullName }
            ))
            put("correspondingAuthor", jacksonObjectMapper().writeValueAsString(
                accountRepository.byEmail(manuscript.correspondingAuthorEmail)?.let { mapOf("type" to "registered", "id" to it.id, "fullName" to it.fullName) }
                    ?: unregisteredAuthorRepository.byEmail(email = manuscript.correspondingAuthorEmail, manuscriptId = manuscript.id)?.let { unregisteredAuthor ->
                        if(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscript.id)
                            || authorizationService.isSectionEditorOnSectionOrSuperior(publication.id, section.id)
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
            ))
            put("files", jacksonObjectMapper().writeValueAsString(manuscriptFileRepository.allFilesByManuscriptId(manuscript.id).map { mapOf("id" to it.id, "name" to it.name) }))
            put("submissionDate", manuscript.submissionDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
            put("publicationDate", manuscript.publicationDate?.format(DateTimeFormatter.ISO_LOCAL_DATE))
            put("isOverseeing", (authorizationService.isSectionEditorOnSectionOrSuperior(publication.id, section.id)
                || authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscript.id))
                || authorizationService.isAdmin)
            put("description", manuscript.description)
            put("state", manuscript.state)
            put("category", categoryRepository.nameById(manuscript.categoryId))
        }
    }
    fun updateState(manuscriptId: Int, newState: ManuscriptState): ResponseEntity<String> {
        val manuscript = manuscriptRepository.byId(manuscriptId) ?: return ResponseEntity.badRequest().body("failed to find manuscript")
        val section = sectionRepository.byId(manuscript.sectionId)!!
        val publication = publicationRepository.by(id = section.publicationId)!!
        val isAdmin = authorizationService.isAdmin
        val isSectionEditorOnSectionOrSuperior = authorizationService.isSectionEditorOnSectionOrSuperior(publication.id, section.id)
        val isEditorOnManuscriptOrAffiliatedSuperior = authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscriptId)
        when(newState) {
            ManuscriptState.ARCHIVED -> {
                if(isSectionEditorOnSectionOrSuperior.not()) return ResponseEntity.status(403).body("forbidden to archive manuscripts")
                if(manuscript.state != ManuscriptState.PUBLISHED) return ResponseEntity.badRequest().body("cannot archive manuscript that is not published")
            }
            ManuscriptState.HIDDEN -> {
                if(isAdmin.not()) return ResponseEntity.status(403).body("forbidden to hide manuscripts")
                if(manuscript.state !in setOf(ManuscriptState.PUBLISHED, ManuscriptState.REJECTED)) return ResponseEntity.badRequest().body("cannot hide manuscript that is not published or rejected")
            }
            ManuscriptState.AWAITING_EIC_REVIEW -> return ResponseEntity.badRequest().body("cannot change manuscript state to ${manuscript.state}")
            ManuscriptState.AWAITING_EDITOR_REVIEW -> {
                if(authorizationService.isEicOnManuscript(manuscriptId).not()) return ResponseEntity.status(403).body("forbidden to editor review manuscript")
                if(manuscript.state != ManuscriptState.AWAITING_EIC_REVIEW) return ResponseEntity.badRequest().body("cannot change state to AWAITING_EDITOR_REVIEW from ${manuscript.state}")
            }
            ManuscriptState.AWAITING_ROUND_INITIALIZATION -> {
                if(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscriptId).not()) return ResponseEntity.status(403).body("forbidden to initialize review round awaiting on manuscript")
                if(manuscript.state !in setOf(ManuscriptState.AWAITING_REVIEWER_REVIEW, ManuscriptState.AWAITING_EDITOR_REVIEW, ManuscriptState.MINOR, ManuscriptState.MAJOR)) return ResponseEntity.badRequest().body("cannot change state to AWAITING_ROUND_INITIALIZATION from ${manuscript.state}")
            }
            ManuscriptState.AWAITING_REVIEWER_REVIEW -> {
                if(isEditorOnManuscriptOrAffiliatedSuperior.not()) return ResponseEntity.status(403).body("forbidden to initialize round on manuscript")
                if(manuscript.state != ManuscriptState.AWAITING_ROUND_INITIALIZATION) return ResponseEntity.badRequest().body("cannot initialize round from ${manuscript.state}")
            }
            ManuscriptState.MINOR, ManuscriptState.MAJOR -> {
                if(isEditorOnManuscriptOrAffiliatedSuperior.not()) return ResponseEntity.status(403).body("forbidden to determine minor/major on manuscript")
                if(manuscript.state !in setOf(ManuscriptState.AWAITING_ROUND_INITIALIZATION, ManuscriptState.AWAITING_REVIEWER_REVIEW)) return ResponseEntity.badRequest().body("cannot initialize round from ${manuscript.state}")
            }
            ManuscriptState.REJECTED -> {
                if(isEditorOnManuscriptOrAffiliatedSuperior.not()) return ResponseEntity.status(403).body("forbidden to reject on manuscript")
                if(manuscript.state.name.contains("AWAITING").not()) return ResponseEntity.badRequest().body("cannot reject manuscript from ${manuscript.state}")
            }
            ManuscriptState.PUBLISHED -> when(manuscript.state) {
                ManuscriptState.HIDDEN -> if(isAdmin.not()) return ResponseEntity.status(403).body("forbidden to unhide manuscripts")
                ManuscriptState.ARCHIVED -> if(isSectionEditorOnSectionOrSuperior.not()) return ResponseEntity.status(403).body("forbidden to unarchive manuscripts")
                ManuscriptState.AWAITING_REVIEWER_REVIEW -> {
                    if(isEditorOnManuscriptOrAffiliatedSuperior.not()) return ResponseEntity.status(403).body("forbidden to publish manuscript")
                }
                else -> return ResponseEntity.badRequest().body("cannot change state to PUBLISHED from ${manuscript.state}")
            }
            ManuscriptState.SNAPSHOT -> return ResponseEntity.badRequest().body("cannot modify snapshot")
        }
        val rowsAffected = manuscriptRepository.updateState(manuscriptId, newState)
        return if(rowsAffected == 1) {
            emailService.sendHtml(
                to = manuscript.correspondingAuthorEmail,
                subject = "Manuscript set to ${newState.name.lowercase()}",
                html = "The manuscript ${manuscript.title} has been set to ${newState.name.lowercase()}."
            )
            ResponseEntity.ok("successfully updated state on manuscript")
        }
        else ResponseEntity.internalServerError().body("failed to update state on manuscript")
    }
}