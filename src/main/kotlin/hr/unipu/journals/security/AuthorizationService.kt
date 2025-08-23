package hr.unipu.journals.security

import hr.unipu.journals.feature.account.Account
import hr.unipu.journals.feature.account.AccountRepository
import hr.unipu.journals.feature.account_role_on_manuscript.AccountRoleOnManuscriptRepository
import hr.unipu.journals.feature.admin.AdminRepository
import hr.unipu.journals.feature.eic_on_publication.EicOnPublicationRepository
import hr.unipu.journals.feature.section_editor_on_section.SectionEditorOnSectionRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service

const val AUTHORIZATION_SERVICE_IS_ROOT = "@authorizationService.isRoot()"
const val AUTHORIZATION_SERVICE_IS_ADMIN = "@authorizationService.isAdmin()"
const val AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION = "@authorizationService.isEicOnPublication(#publicationId) || @authorizationService.isAdmin()"
const val AUTHORIZATION_SERVICE_IS_EIC_ON_MANUSCRIPT = "@authorizationService.isEicOnManuscript(#manuscriptId)"
const val AUTHORIZATION_SERVICE_IS_SECTION_EDITOR_ON_SECTION = "@authorizationService.isAdmin() || @authorizationService.isEicOnPublication(#publicationId) || @authorizationService.isSectionEditorOnSection(#sectionId)"
const val AUTHORIZATION_SERVICE_IS_EDITOR_ON_MANUSCRIPT = "@authorizationService.isAdmin() || @authorizationService.isEicOnPublication(#publicationId) || @authorizationService.isSectionEditorOnSection(#sectionId) || @authorizationService.isEditorOnManuscript(#manuscriptId)"
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
    private val user get(): User? {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("No authentication found in security context")

        if(authentication.principal == "anonymousUser") return null

        return authentication.principal as? User
            ?: throw IllegalStateException("Principal is not an instance of User, principal: ${authentication.principal}")
    }
    private val account get(): Account? = user?.let { accountRepository.byEmail(it.username) } // ?: throw IllegalStateException("email ${it.username} does not exist") // maybe add to logs

    fun isRoot(): Boolean = user?.username == "root@unipu.hr"
    fun isAdmin(): Boolean = account?.let { adminRepository.isAdmin(it.email) } ?: false
    fun isEicOnPublication(publicationId: Int): Boolean = account?.id?.let { eicOnPublicationRepository.isEicOnPublication(it, publicationId) } ?: false
    fun isEicOnManuscript(manuscriptId: Int): Boolean = account?.id?.let { accountRoleOnManuscriptRepository.isEicOnManuscript(it, manuscriptId) } ?: false
    fun isSectionEditorOnSection(sectionId: Int): Boolean = account?.id?.let { sectionEditorOnSectionRepository.isSectionEditorOnSection(it, sectionId) } ?: false
    fun isEditorOnManuscript(manuscriptId: Int): Boolean = account?.id?.let { accountRoleOnManuscriptRepository.isEditorOnManuscript(it, manuscriptId) } ?: false
    fun isReviewerOnManuscript(manuscriptId: Int): Boolean = account?.id?.let { accountRoleOnManuscriptRepository.isReviewerOnManuscript(it, manuscriptId) } ?: false
    fun isCorrespondingAuthorOnManuscript(manuscriptId: Int): Boolean = account?.id?.let { accountRoleOnManuscriptRepository.isCorrespondingAuthorOnManuscript(it, manuscriptId) } ?: false
    fun isAuthorOnManuscript(manuscriptId: Int): Boolean = account?.id?.let { accountRoleOnManuscriptRepository.isAuthorOnManuscript(it, manuscriptId) } ?: false
}