package hr.unipu.journals.security

import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.eic_on_publication.EicOnPublicationRepository
import hr.unipu.journals.feature.manuscript_review.ManuscriptReviewRepository
import hr.unipu.journals.feature.section_editor_on_section.SectionEditorOnSectionRepository
import org.springframework.stereotype.Service

@Service
class RoleContextService(
    private val accountRepository: AccountRepository,
    private val eicOnPublicationRepository: EicOnPublicationRepository,
    private val sectionEditorOnSectionRepository: SectionEditorOnSectionRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
    private val manuscriptReviewRepository: ManuscriptReviewRepository
) {
    fun isEiCOnPublication(eicId: Int, publicationId: Int): Boolean {
        return eicOnPublicationRepository.isEicOnPublication(eicId, publicationId)
    }
    fun isEicOnManuscript(eicId: Int, manuscriptId: Int) {
        return accountRoleOnManuscriptRepository.isEicOnManuscript(eicId, manuscriptId)
    }
    fun isSectionEditorOnSection() {}
    fun isEditorOnManuscript() {}
    fun isReviewerOnManuscript() {}
    fun isCorrespondingAuthorOnManuscript() {}
    fun isAuthorOnManuscript() {}

}