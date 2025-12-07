package hr.unipu.journals.feature.invite

import hr.unipu.journals.feature.manuscript.core.Manuscript
import hr.unipu.journals.feature.manuscript.core.ManuscriptState
import hr.unipu.journals.feature.publication.core.Publication
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import java.time.LocalDateTime

@DataJdbcTest
class InviteRepositoryTests {
    @Autowired private lateinit var inviteRepository: InviteRepository
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @Test fun `is admin`() {
        Assertions.assertTrue(inviteRepository.isAdmin("invited.admin1@unipu.hr"))
        Assertions.assertTrue(inviteRepository.isAdmin("invited.admin2@unipu.hr"))
        Assertions.assertFalse(inviteRepository.isAdmin("invited.eic.on.publication1@unipu.hr"))
        Assertions.assertFalse(inviteRepository.isAdmin("invited.eic.on.publication2@unipu.hr"))
        Assertions.assertFalse(inviteRepository.isAdmin("invited.reviewer.on.manuscript1@unipu.hr"))
        Assertions.assertFalse(inviteRepository.isAdmin("invited.reviewer.on.manuscript2@unipu.hr"))
    }
    @Test fun `retrieve all admin emails`() {
        assertEquals(
            listOf("invited.admin1@unipu.hr", "invited.admin2@unipu.hr"),
            inviteRepository.emailsByTarget(InvitationTarget.ADMIN)
        )
    }
    @Test fun `retrieve eic on publication emails by publication id`() {
        assertEquals(
            listOf("invited.eic.on.publication1@unipu.hr"),
            inviteRepository.emailsByTarget(InvitationTarget.EIC_ON_PUBLICATION, 1)
        )
        assertEquals(
            listOf("invited.eic.on.publication2@unipu.hr"),
            inviteRepository.emailsByTarget(InvitationTarget.EIC_ON_PUBLICATION, 2)
        )
    }
    @Test fun `retrieve section editors on section emails by section id`() {
        assertEquals(
            listOf("invited.section.editor.on.section1@unipu.hr"),
            inviteRepository.emailsByTarget(InvitationTarget.SECTION_EDITOR_ON_SECTION, 1)
        )
        assertEquals(
            listOf("invited.section.editor.on.section2@unipu.hr"),
            inviteRepository.emailsByTarget(InvitationTarget.SECTION_EDITOR_ON_SECTION, 2)
        )
    }
    @Test fun `retrieve all publications under review with affiliation by email`() {
        assertEquals(
            listOf(
                Publication(2, "Nature of Biology", false),
                Publication(3, "Physics Letters", false),
            ),
            inviteRepository.allPublicationsContainingPendingManuscripts("invited.manuscript.role.1.2.3@unipu.hr")
        )
    }
    @Test fun `retrieve all manuscripts with affiliation by email`() {
        assertEquals(
            listOf(
                Manuscript(
                    id = 2,
                    title = "Deep Learning in Genomics",
                    description = "Analyzes genomic sequences using deep neural networks to predict mutations.",
                    categoryId = 1,
                    state = ManuscriptState.AWAITING_EDITOR_REVIEW,
                    sectionId = 7,
                    downloadUrl = "http://example.com/ms2.pdf",
                    submissionDate = LocalDateTime.of(2023, 9, 28, 13, 28, 0),
                    publicationDate = null,
                ),
                Manuscript(
                    id = 3,
                    title = "Natural Language Processing in Clinical Notes",
                    description = "Extracting insights from unstructured clinical data using NLP.",
                    categoryId = 2,
                    state = ManuscriptState.AWAITING_REVIEWER_REVIEW,
                    sectionId = 11,
                    downloadUrl = "http://example.com/ms3.pdf",
                    submissionDate = LocalDateTime.of(2022, 9, 28, 13, 28, 0),
                    publicationDate = null,
                )
            ),
            inviteRepository.affiliatedManuscripts("invited.manuscript.role.1.2.3@unipu.hr")
        )
    }
    @Test fun `invite admin`() {
        inviteRepository.invite("test.invite.admin@unipu.hr", InvitationTarget.ADMIN)
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.admin@unipu.hr' AND target = 'ADMIN')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.admin@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.admin@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.admin@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.admin@unipu.hr' AND target = 'EDITOR_ON_MANUSCRIPT')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.admin@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
    }
    @Test fun `invite eic on publication`() {
        inviteRepository.invite("test.invite.eic.on.publication@unipu.hr", InvitationTarget.EIC_ON_PUBLICATION)
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.publication@unipu.hr' AND target = 'ADMIN')"))
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.publication@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.publication@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.publication@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.publication@unipu.hr' AND target = 'EDITOR_ON_MANUSCRIPT')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.publication@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
    }
    @Test fun `invite section editor on section`() {
        inviteRepository.invite("test.invite.section.editor.on.section@unipu.hr", InvitationTarget.SECTION_EDITOR_ON_SECTION)
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.section.editor.on.section@unipu.hr' AND target = 'ADMIN')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.section.editor.on.section@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.section.editor.on.section@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.section.editor.on.section@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.section.editor.on.section@unipu.hr' AND target = 'EDITOR_ON_MANUSCRIPT')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.section.editor.on.section@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
    }
    @Test fun `invite EiC on manuscript`() {
        inviteRepository.invite("test.invite.eic.on.manuscript@unipu.hr", InvitationTarget.EIC_ON_MANUSCRIPT)
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.manuscript@unipu.hr' AND target = 'ADMIN')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.manuscript@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.manuscript@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.manuscript@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.manuscript@unipu.hr' AND target = 'EDITOR_ON_MANUSCRIPT')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.manuscript@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
    }
    @Test fun `invite editor on manuscript`() {
        inviteRepository.invite("test.invite.editor.on.manuscript@unipu.hr", InvitationTarget.EDITOR_ON_MANUSCRIPT)
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.editor.on.manuscript@unipu.hr' AND target = 'ADMIN')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.editor.on.manuscript@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.editor.on.manuscript@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.editor.on.manuscript@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.editor.on.manuscript@unipu.hr' AND target = 'EDITOR_ON_MANUSCRIPT')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.editor.on.manuscript@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
    }
    @Test fun `invite reviewer on manuscript`() {
        inviteRepository.invite("test.invite.reviewer.on.manuscript@unipu.hr", InvitationTarget.REVIEWER_ON_MANUSCRIPT)
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.reviewer.on.manuscript@unipu.hr' AND target = 'ADMIN')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.reviewer.on.manuscript@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.reviewer.on.manuscript@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.reviewer.on.manuscript@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.reviewer.on.manuscript@unipu.hr' AND target = 'EDITOR_ON_MANUSCRIPT')"))
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.reviewer.on.manuscript@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
    }
    @Test fun `revoke admin invite`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.admin1@unipu.hr' AND target = 'ADMIN')"))
        inviteRepository.revoke("invited.admin1@unipu.hr", InvitationTarget.ADMIN)
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.admin1@unipu.hr' AND target = 'ADMIN')"))
    }
    @Test fun `revoke eic on publication invite`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.eic.on.publication1@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
        inviteRepository.revoke("invited.eic.on.publication1@unipu.hr", InvitationTarget.EIC_ON_PUBLICATION)
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.eic.on.publication1@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
    }
    @Test fun `revoke section editor on section invite`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.section.editor.on.section1@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
        inviteRepository.revoke("invited.section.editor.on.section1@unipu.hr", InvitationTarget.SECTION_EDITOR_ON_SECTION)
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.section.editor.on.section1@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
    }
    @Test fun `revoke eic on manuscript invite`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.eic.on.manuscript1@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
        inviteRepository.revoke("invited.eic.on.manuscript1@unipu.hr", InvitationTarget.EIC_ON_MANUSCRIPT)
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.eic.on.manuscript1@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
    }
    @Test fun `revoke editor on manuscript invite`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.editor.on.manuscript1@unipu.hr' AND target = 'EDITOR_ON_MANUSCRIPT')"))
        inviteRepository.revoke("invited.editor.on.publication1@unipu.hr", InvitationTarget.EDITOR_ON_MANUSCRIPT)
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.editor.on.manuscript1@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
    }
    @Test fun `revoke reviewer on manuscript invite`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.reviewer.on.manuscript1@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
        inviteRepository.revoke("invited.reviewer.on.manuscript1@unipu.hr", InvitationTarget.REVIEWER_ON_MANUSCRIPT)
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.reviewer.on.manuscript1@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
    }
}