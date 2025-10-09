package hr.unipu.journals.data_jdbc_tests

import hr.unipu.journals.feature.invite.InvitationTarget
import hr.unipu.journals.feature.invite.InviteRepository
import hr.unipu.journals.feature.manuscript.Manuscript
import hr.unipu.journals.feature.manuscript.ManuscriptState
import hr.unipu.journals.feature.publication.Publication
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

@DataJdbcTest
class InviteRepositoryTests {
    @Autowired private lateinit var inviteRepository: InviteRepository
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @Test fun `is admin`() {
        assertTrue(inviteRepository.isAdmin("invited.admin1@unipu.hr"))
        assertTrue(inviteRepository.isAdmin("invited.admin2@unipu.hr"))
        assertFalse(inviteRepository.isAdmin("invited.eic.on.publication1@unipu.hr"))
        assertFalse(inviteRepository.isAdmin("invited.eic.on.publication2@unipu.hr"))
        assertFalse(inviteRepository.isAdmin("invited.reviewer.on.manuscript1@unipu.hr"))
        assertFalse(inviteRepository.isAdmin("invited.reviewer.on.manuscript2@unipu.hr"))
    }
    @Test fun `retrieve all admin emails`() {
        assertEquals(listOf("invited.admin1@unipu.hr", "invited.admin2@unipu.hr"), inviteRepository.emailsByTarget(InvitationTarget.ADMIN))
    }
    @Test fun `retrieve eic on publication emails by publication id`() {
        assertEquals(listOf("invited.eic.on.publication1@unipu.hr"), inviteRepository.emailsByTarget(InvitationTarget.EIC_ON_PUBLICATION, 1))
        assertEquals(listOf("invited.eic.on.publication2@unipu.hr"), inviteRepository.emailsByTarget(InvitationTarget.EIC_ON_PUBLICATION, 2))
    }
    @Test fun `retrieve section editors on section emails by section id`() {
        assertEquals(listOf("invited.section.editor.on.section1@unipu.hr"), inviteRepository.emailsByTarget(InvitationTarget.SECTION_EDITOR_ON_SECTION, 1))
        assertEquals(listOf("invited.section.editor.on.section2@unipu.hr"), inviteRepository.emailsByTarget(InvitationTarget.SECTION_EDITOR_ON_SECTION, 2))
    }
    @Test fun `retrieve all publications under review with affiliation by email`() {
        assertEquals(
            listOf(
                Publication(2, "Nature of Biology", false),
                Publication(3, "Physics Letters", false),
            ),
            inviteRepository.allPublicationsWhichContainManuscriptsUnderReviewWithAffiliation("invited.manuscript.role.1.2.3@unipu.hr")
        )
    }
    @Test fun `retrieve all manuscripts with affiliation by email`() {
        assertEquals(
            listOf(
                Manuscript(2, "Deep Learning in Genomics", "Analyzes genomic sequences using deep neural networks to predict mutations.", 11, 1, ManuscriptState.AWAITING_INITIAL_EDITOR_REVIEW, 7, "http://example.com/ms2.pdf", LocalDateTime.of(2023, 9, 28, 13, 28, 0), null, 245, 33),
                Manuscript(3, "Natural Language Processing in Clinical Notes", "Extracting insights from unstructured clinical data using NLP.", 10, 2, ManuscriptState.AWAITING_REVIEWER_REVIEW, 11, "http://example.com/ms3.pdf", LocalDateTime.of(2022, 9, 28, 13, 28, 0), null, 310, 47)
            ),
            inviteRepository.affiliatedManuscripts("invited.manuscript.role.1.2.3@unipu.hr")
        )
    }
    @Test fun `invite admin`() {
        inviteRepository.insert("test.invite.admin@unipu.hr", InvitationTarget.ADMIN)
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.admin@unipu.hr' AND target = 'ADMIN')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.admin@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.admin@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.admin@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.admin@unipu.hr' AND target = 'EDITOR_ON_MANUSCRIPT')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.admin@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
    }
    @Test fun `invite eic on publication`() {
        inviteRepository.insert("test.invite.eic.on.publication@unipu.hr", InvitationTarget.EIC_ON_PUBLICATION)
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.publication@unipu.hr' AND target = 'ADMIN')"))
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.publication@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.publication@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.publication@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.publication@unipu.hr' AND target = 'EDITOR_ON_MANUSCRIPT')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.publication@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
    }
    @Test fun `invite section editor on section`() {
        inviteRepository.insert("test.invite.section.editor.on.section@unipu.hr", InvitationTarget.SECTION_EDITOR_ON_SECTION)
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.section.editor.on.section@unipu.hr' AND target = 'ADMIN')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.section.editor.on.section@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.section.editor.on.section@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.section.editor.on.section@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.section.editor.on.section@unipu.hr' AND target = 'EDITOR_ON_MANUSCRIPT')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.section.editor.on.section@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
    }
    @Test fun `invite EiC on manuscript`() {
        inviteRepository.insert("test.invite.eic.on.manuscript@unipu.hr", InvitationTarget.EIC_ON_MANUSCRIPT)
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.manuscript@unipu.hr' AND target = 'ADMIN')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.manuscript@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.manuscript@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.manuscript@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.manuscript@unipu.hr' AND target = 'EDITOR_ON_MANUSCRIPT')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.eic.on.manuscript@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
    }
    @Test fun `invite editor on manuscript`() {
        inviteRepository.insert("test.invite.editor.on.manuscript@unipu.hr", InvitationTarget.EDITOR_ON_MANUSCRIPT)
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.editor.on.manuscript@unipu.hr' AND target = 'ADMIN')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.editor.on.manuscript@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.editor.on.manuscript@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.editor.on.manuscript@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.editor.on.manuscript@unipu.hr' AND target = 'EDITOR_ON_MANUSCRIPT')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.editor.on.manuscript@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
    }
    @Test fun `invite reviewer on manuscript`() {
        inviteRepository.insert("test.invite.reviewer.on.manuscript@unipu.hr", InvitationTarget.REVIEWER_ON_MANUSCRIPT)
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.reviewer.on.manuscript@unipu.hr' AND target = 'ADMIN')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.reviewer.on.manuscript@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.reviewer.on.manuscript@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.reviewer.on.manuscript@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.reviewer.on.manuscript@unipu.hr' AND target = 'EDITOR_ON_MANUSCRIPT')"))
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'test.invite.reviewer.on.manuscript@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
    }
    @Test fun `revoke admin invite`() {
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.admin1@unipu.hr' AND target = 'ADMIN')"))
        inviteRepository.revoke("invited.admin1@unipu.hr", InvitationTarget.ADMIN)
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.admin1@unipu.hr' AND target = 'ADMIN')"))
    }
    @Test fun `revoke eic on publication invite`() {
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.eic.on.publication1@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
        inviteRepository.revoke("invited.eic.on.publication1@unipu.hr", InvitationTarget.EIC_ON_PUBLICATION)
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.eic.on.publication1@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
    }
    @Test fun `revoke section editor on section invite`() {
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.section.editor.on.section1@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
        inviteRepository.revoke("invited.section.editor.on.section1@unipu.hr", InvitationTarget.SECTION_EDITOR_ON_SECTION)
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.section.editor.on.section1@unipu.hr' AND target = 'SECTION_EDITOR_ON_SECTION')"))
    }
    @Test fun `revoke eic on manuscript invite`() {
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.eic.on.manuscript1@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
        inviteRepository.revoke("invited.eic.on.manuscript1@unipu.hr", InvitationTarget.EIC_ON_MANUSCRIPT)
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.eic.on.manuscript1@unipu.hr' AND target = 'EIC_ON_MANUSCRIPT')"))
    }
    @Test fun `revoke editor on manuscript invite`() {
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.editor.on.manuscript1@unipu.hr' AND target = 'EDITOR_ON_MANUSCRIPT')"))
        inviteRepository.revoke("invited.editor.on.publication1@unipu.hr", InvitationTarget.EDITOR_ON_MANUSCRIPT)
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.editor.on.manuscript1@unipu.hr' AND target = 'EIC_ON_PUBLICATION')"))
    }
    @Test fun `revoke reviewer on manuscript invite`() {
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.reviewer.on.manuscript1@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
        inviteRepository.revoke("invited.reviewer.on.manuscript1@unipu.hr", InvitationTarget.REVIEWER_ON_MANUSCRIPT)
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM invite WHERE email = 'invited.reviewer.on.manuscript1@unipu.hr' AND target = 'REVIEWER_ON_MANUSCRIPT')"))
    }
}