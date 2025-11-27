package hr.unipu.journals.view.home

import hr.unipu.journals.feature.invite.InvitationTarget
import hr.unipu.journals.feature.invite.InviteRepository
import hr.unipu.journals.feature.section.core.SectionRepository
import hr.unipu.journals.feature.section.section_editor_on_section.SectionEditorOnSectionRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ManageSectionEditorOnSectionPageController(
    private val sectionRepository: SectionRepository,
    private val sectionEditorOnSectionRepository: SectionEditorOnSectionRepository,
    private val inviteRepository: InviteRepository
) {
    @GetMapping("/publication/{publicationId}/section/{sectionId}/manage-section-editor-on-section")
    fun page(
        @PathVariable publicationId: Int,
        @PathVariable sectionId: Int,
        model: Model
    ): String {
        model["currentSection"] = sectionRepository.byId(sectionId).title
        model["sectionEditorEmails"] = sectionEditorOnSectionRepository.sectionEditorEmailsBySectionId(sectionId) + inviteRepository.emailsByTarget(InvitationTarget.SECTION_EDITOR_ON_SECTION, sectionId)
        return "home/manage/manage-section-editor-on-section-page"
    }
}
