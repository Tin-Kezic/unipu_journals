package hr.unipu.journals.view.review

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.manuscript.account_role_on_manuscript.ManuscriptRole
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptState
import hr.unipu.journals.feature.manuscript.file.ManuscriptFileRepository
import hr.unipu.journals.feature.manuscript.review.ManuscriptReviewRepository
import hr.unipu.journals.feature.publication.core.PublicationRepository
import hr.unipu.journals.feature.section.core.SectionRepository
import hr.unipu.journals.feature.unregistered_author.UnregisteredAuthorRepository
import hr.unipu.journals.security.AUTHORIZATION_SERVICE_IS_EDITOR_ON_MANUSCRIPT_OR_SUPERIOR
import hr.unipu.journals.security.AuthorizationService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.time.format.DateTimeFormatter

@Controller
class ReviewPagesController(
    private val authorizationService: AuthorizationService,
    private val accountRepository: AccountRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val unregisteredAuthorRepository: UnregisteredAuthorRepository,
    private val publicationRepository: PublicationRepository,
    private val sectionRepository: SectionRepository,
    private val manuscriptReviewRepository: ManuscriptReviewRepository,
    private val manuscriptRepository: ManuscriptRepository,
    private val manuscriptFileRepository: ManuscriptFileRepository
) {
    @GetMapping("/review/{manuscriptId}")
    @PreAuthorize(AUTHORIZATION_SERVICE_IS_EDITOR_ON_MANUSCRIPT_OR_SUPERIOR)
    fun page(@PathVariable manuscriptId: Int, model: Model): String {
        val manuscript = manuscriptRepository.byId(manuscriptId) ?: throw IllegalArgumentException("failed to find manuscript $manuscriptId")
        val sectionId = sectionRepository.byId(manuscript.sectionId)!!.id
        val publicationId = publicationRepository.by(id = sectionId)!!.id
        val accountId = authorizationService.account!!.id
        model["manuscript"] = Triple(
            manuscript,
            accountRoleOnManuscriptRepository.all(role = ManuscriptRole.AUTHOR, manuscriptId = manuscript.id).map { accountRepository.byId(it.accountId)!! },
            unregisteredAuthorRepository.authors(manuscript.id)
        ).let { (manuscript, registeredAuthors, unregisteredAuthors) -> buildMap {
            put("id", manuscript.id)
            put("roles", jacksonObjectMapper().writeValueAsString(
                accountRoleOnManuscriptRepository.all(manuscriptId = manuscript.id, accountId = accountId).map { it.accountRole }
            ))
            put("title", manuscript.title)
            put("registeredAuthors", jacksonObjectMapper().writeValueAsString(
                registeredAuthors.map { author -> mapOf("id" to author.id, "fullName" to author.fullName) }
            ))
            put("unregisteredAuthors", jacksonObjectMapper().writeValueAsString(
                if(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscript.id)
                    || authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)
                    || authorizationService.isAdmin)
                    unregisteredAuthors else unregisteredAuthors.map { author -> author.fullName }
            ))
            put("correspondingAuthor", jacksonObjectMapper().writeValueAsString(
                accountRepository.byEmail(manuscript.correspondingAuthorEmail)?.let { mapOf("type" to "registered", "id" to it.id, "fullName" to it.fullName) }
                    ?: unregisteredAuthorRepository.byEmail(manuscript.correspondingAuthorEmail)?.let { unregisteredAuthor ->
                        if(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscript.id)
                            || authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)
                            || authorizationService.isAdmin)
                            unregisteredAuthor.let { author -> mapOf(
                                "type" to "unregistered",
                                "manuscriptId" to author.id,
                                "fullName" to author.fullName,
                                "email" to author.email,
                                "country" to author.country,
                                "affiliation" to author.affiliation,
                                "manuscriptId" to author.manuscriptId
                            )}
                        else unregisteredAuthor.fullName
                    }
            ))
            put("files", jacksonObjectMapper().writeValueAsString(
                manuscriptFileRepository.allFilesByManuscriptId(manuscript.id).map { mapOf("id" to it.id, "name" to it.name) }
            ))
            put("submissionDate", manuscript.submissionDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
            put("isOverseeing", (authorizationService.isSectionEditorOnSectionOrSuperior(publicationId, sectionId)
                    || authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscript.id))
                    || authorizationService.isAdmin)
            put("description", manuscript.description)
            put("state", manuscript.state)
        }}
        return when(manuscript.state) {
            ManuscriptState.AWAITING_EIC_REVIEW -> {
                require(authorizationService.isEicOnManuscript(manuscriptId))
                model["type"] = "EIC"
                "/review/initial"
            }
            ManuscriptState.AWAITING_EDITOR_REVIEW -> {
                require(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscriptId))
                model["type"] = "EDITOR"
                "/review/initial"
            }
            ManuscriptState.AWAITING_ROUND_INITIALIZATION -> {
                require(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscriptId))
                model["type"] = if(authorizationService.isEicOnManuscript(manuscriptId)) "EIC" else "EDITOR"
                val registeredReviewers = accountRoleOnManuscriptRepository.all(role = ManuscriptRole.REVIEWER, manuscriptId = manuscriptId)
                        .map { accountRepository.byId(it.accountId)!! }
                        .map { it.email }
                val unregisteredReviewers = inviteRepository.all(target = InvitationTarget.REVIEWER, targetId = manuscriptId).map { it.email }
                model["reviewers"] = registeredReviewers + unregisteredReviewers
                val round = manuscriptReviewRoundRepository.by(manuscriptId)
                model["editorRecommendation"] = round?.editorRecommendation ?: ""
                model["editorComment"] = round?.editorComment ?: ""
                "/review/round-initialization-page"
            }
            ManuscriptState.AWAITING_REVIEWER_REVIEW -> {
                require(authorizationService.isReviewerOnManuscriptOrAffiliatedSuperior(manuscriptId))
                model["type"] = if(authorizationService.isEicOnManuscript(manuscriptId)) "EIC" else if(authorizationService.isEditorOnManuscriptOrAffiliatedSuperior(manuscriptId)) "EDITOR" else null
                "/review/review-page"
            }
            else -> throw IllegalArgumentException("manuscript is currently not under review")
        }
    }
}