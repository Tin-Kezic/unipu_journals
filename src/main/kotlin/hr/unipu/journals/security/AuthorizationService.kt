package hr.unipu.journals.security

import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.eic_on_publication.EicOnPublicationRepository
import hr.unipu.journals.feature.section_editor_on_section.SectionEditorOnSectionRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service

const val AUTHORIZATION_SERVICE_IS_ROOT = "@authorizationService.isRoot()"
const val AUTHORIZATION_SERVICE_IS_ADMIN = "@authorizationService.isAdmin()"
const val AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION = "@authorizationService.isEicOnPublication(#publicationId)"
const val AUTHORIZATION_SERVICE_IS_EIC_ON_MANUSCRIPT = "@authorizationService.isEicOnManuscript(#manuscriptId)"
const val AUTHORIZATION_SERVICE_IS_SECTION_EDITOR_ON_SECTION = "@authorizationService.isSectionEditorOnSection(#sectionId)"
const val AUTHORIZATION_SERVICE_IS_EDITOR_ON_MANUSCRIPT = "@authorizationService.isEditorOnManuscript(#manuscriptId)"
const val AUTHORIZATION_SERVICE_IS_REVIEWER_ON_MANUSCRIPT = "@authorizationService.isReviewerOnManuscript(#manuscriptId)"
const val AUTHORIZATION_SERVICE_IS_CORRESPONDING_AUTHOR_ON_MANUSCRIPT = "@authorizationService.isCorrespondingAuthorOnManuscript(#manuscriptId)"
const val AUTHORIZATION_SERVICE_IS_AUTHOR_ON_MANUSCRIPT = "@authorizationService.isAuthorOnManuscript(#manuscriptId)"

@Service
class AuthorizationService(
    private val accountRepository: AccountRepository,
    private val adminRepository: AdminRepository,
    private val eicOnPublicationRepository: EicOnPublicationRepository,
    private val sectionEditorOnSectionRepository: SectionEditorOnSectionRepository,
    private val accountRoleOnManuscriptRepository: AccountRoleOnManuscriptRepository,
) {
    private fun user(): User {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("No authentication found in security context")

        return authentication.principal as? User
            ?: throw IllegalStateException("Principal is not an instance of User, principal: ${authentication.principal}")
    }
    private val account get() = accountRepository.byEmail(user().username) ?: throw IllegalStateException("email ${user().username} does not exist")

    fun isRoot(): Boolean = user().username == "root@unipu.hr"
    fun isAdmin(): Boolean = account.isAdmin
    fun isEicOnPublication(publicationId: Int) = eicOnPublicationRepository.isEicOnPublication(account.id, publicationId)
    fun isEicOnManuscript(manuscriptId: Int) = accountRoleOnManuscriptRepository.isEicOnManuscript(account.id, manuscriptId)
    fun isSectionEditorOnSection(sectionId: Int) = sectionEditorOnSectionRepository.isSectionEditorOnSection(account.id, sectionId)
    fun isEditorOnManuscript(manuscriptId: Int) = accountRoleOnManuscriptRepository.isEditorOnManuscript(account.id, manuscriptId)
    fun isReviewerOnManuscript(manuscriptId: Int) = accountRoleOnManuscriptRepository.isReviewerOnManuscript(account.id, manuscriptId)
    fun isCorrespondingAuthorOnManuscript(manuscriptId: Int) = accountRoleOnManuscriptRepository.isCorrespondingAuthorOnManuscript(account.id, manuscriptId)
    fun isAuthorOnManuscript(manuscriptId: Int) = accountRoleOnManuscriptRepository.isAuthorOnManuscript(account.id, manuscriptId)
}