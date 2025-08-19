package hr.unipu.journals.security

import hr.unipu.journals.feature.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.eic_on_publication.EicOnPublicationRepository
import hr.unipu.journals.feature.section_editor_on_section.SectionEditorOnSectionRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
/*
private fun user(): SecurityUser = (SecurityContextHolder.getContext().authentication.principal as? SecurityUser) ?: throw IllegalStateException("Current user is not authenticated")

@Service
class AuthorizationService(
    private val eicOnPublicationRepository: EicOnPublicationRepository,
    private val sectionEditorOnSectionRepository: SectionEditorOnSectionRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
) {
    fun isRoot(): Boolean = user().username == "root@unipu.hr"
    fun isAdmin(): Boolean = user().isAdmin
    fun isEicOnPublication(publicationId: Int): Boolean {
        return eicOnPublicationRepository.isEicOnPublication(user().id, publicationId)
    }
    fun isEicOnManuscript(manuscriptId: Int): Boolean {
        return accountRoleOnManuscriptRepository.isEicOnManuscript(user().id, manuscriptId)
    }
    fun isSectionEditorOnSection(sectionId: Int): Boolean {
        return sectionEditorOnSectionRepository.isSectionEditorOnSection(user().id, sectionId)
    }
    fun isEditorOnManuscript(manuscriptId: Int): Boolean {
        return accountRoleOnManuscriptRepository.isEditorOnManuscript(user().id, manuscriptId)
    }
    fun isReviewerOnManuscript(manuscriptId: Int): Boolean {
        return accountRoleOnManuscriptRepository.isReviewerOnManuscript(user().id, manuscriptId)
    }
    fun isCorrespondingAuthorOnManuscript(manuscriptId: Int): Boolean {
        return accountRoleOnManuscriptRepository.isCorrespondingAuthorOnManuscript(user().id, manuscriptId)
    }
    fun isAuthorOnManuscript(manuscriptId: Int): Boolean {
        return accountRoleOnManuscriptRepository.isAuthorOnManuscript(user().id, manuscriptId)
    }
}
 */