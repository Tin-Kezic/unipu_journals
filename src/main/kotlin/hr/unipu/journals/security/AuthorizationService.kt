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
const val AUTHORIZATION_SERVICE_IS_EIC_ON_PUBLICATION_OR_SUPERIOR = "@authorizationService.isEicOnPublicationOrSuperiorOrSuperior(#publicationId)"
const val AUTHORIZATION_SERVICE_IS_EIC_ON_MANUSCRIPT_OR_SUPERIOR = "@authorizationService.isEicOnManuscriptOrSuperior(#manuscriptId)"
const val AUTHORIZATION_SERVICE_IS_SECTION_EDITOR_ON_SECTION_OR_SUPERIOR = "@authorizationService.isSectionEditorOnSectionOrSuperior(#sectionId)"
const val AUTHORIZATION_SERVICE_IS_EDITOR_ON_MANUSCRIPT_OR_SUPERIOR = "@authorizationService.isEditorOnManuscriptOrSuperior(#manuscriptId)"
const val AUTHORIZATION_SERVICE_IS_REVIEWER_ON_MANUSCRIPT_OR_SUPERIOR = "@authorizationService.isReviewerOnManuscriptOrSuperior(#manuscriptId)"
const val AUTHORIZATION_SERVICE_IS_CORRESPONDING_AUTHOR_ON_MANUSCRIPT_OR_SUPERIOR = "@authorizationService.isCorrespondingAuthorOnManuscriptOrSuperior(#manuscriptId)"
const val AUTHORIZATION_SERVICE_IS_AUTHOR_ON_MANUSCRIPT_OR_SUPERIOR = "@authorizationService.isAuthorOnManuscriptOrSuperior(#manuscriptId)"

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
    fun isEicOnPublicationOrSuperior(publicationId: Int): Boolean = account?.let {
        eicOnPublicationRepository.isEicOnPublication(it.id, publicationId) || adminRepository.isAdmin(it.email)
    } ?: false
    fun isEicOnManuscriptOrSuperior(manuscriptId: Int): Boolean = account?.id?.let { accountRoleOnManuscriptRepository.isEicOnManuscript(it, manuscriptId) } ?: false
    fun isSectionEditorOnSectionOrSuperior(publicationId: Int, sectionId: Int): Boolean = account?.let {
        sectionEditorOnSectionRepository.isSectionEditorOnSection(it.id, sectionId)
                || eicOnPublicationRepository.isEicOnPublication(it.id, publicationId)
                || adminRepository.isAdmin(it.email)
    } ?: false
    fun isEditorOnManuscriptOrSuperior(publicationId: Int, sectionId: Int, manuscriptId: Int): Boolean = account?.let {
        accountRoleOnManuscriptRepository.isEditorOnManuscript(it.id, manuscriptId)
                || sectionEditorOnSectionRepository.isSectionEditorOnSection(it.id, sectionId)
                || eicOnPublicationRepository.isEicOnPublication(it.id, publicationId)
                || adminRepository.isAdmin(it.email)
    } ?: false
    fun isReviewerOnManuscriptOrSuperior(publicationId: Int, sectionId: Int, manuscriptId: Int): Boolean = account?.let {
        accountRoleOnManuscriptRepository.isReviewerOnManuscript(it.id, manuscriptId)
                || accountRoleOnManuscriptRepository.isEditorOnManuscript(it.id, manuscriptId)
                || sectionEditorOnSectionRepository.isSectionEditorOnSection(it.id, sectionId)
                || eicOnPublicationRepository.isEicOnPublication(it.id, publicationId)
                || adminRepository.isAdmin(it.email)
    } ?: false
    fun isCorrespondingAuthorOnManuscriptOrSuperior(publicationId: Int, sectionId: Int, manuscriptId: Int): Boolean = account?.let {
        accountRoleOnManuscriptRepository.isCorrespondingAuthorOnManuscript(it.id, manuscriptId)
                || accountRoleOnManuscriptRepository.isEditorOnManuscript(it.id, manuscriptId)
                || sectionEditorOnSectionRepository.isSectionEditorOnSection(it.id, sectionId)
                || eicOnPublicationRepository.isEicOnPublication(it.id, publicationId)
                || adminRepository.isAdmin(it.email)
    } ?: false
    fun isAuthorOnManuscriptOrSuperior(publicationId: Int, sectionId: Int, manuscriptId: Int): Boolean = account?.let {
        accountRoleOnManuscriptRepository.isAuthorOnManuscript(it.id, manuscriptId)
                || accountRoleOnManuscriptRepository.isCorrespondingAuthorOnManuscript(it.id, manuscriptId)
                || accountRoleOnManuscriptRepository.isEditorOnManuscript(it.id, manuscriptId)
                || sectionEditorOnSectionRepository.isSectionEditorOnSection(it.id, sectionId)
                || eicOnPublicationRepository.isEicOnPublication(it.id, publicationId)
                || adminRepository.isAdmin(it.email)
    } ?: false
}