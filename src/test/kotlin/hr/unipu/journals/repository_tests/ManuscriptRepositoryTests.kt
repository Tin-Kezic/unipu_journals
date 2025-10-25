package hr.unipu.journals.repository_tests

import hr.unipu.journals.feature.manuscript.core.Manuscript
import hr.unipu.journals.feature.manuscript.core.ManuscriptRepository
import hr.unipu.journals.feature.manuscript.core.ManuscriptState
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
class ManuscriptRepositoryTests {
    @Autowired private lateinit var manuscriptRepository: ManuscriptRepository
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @Test fun `increment views by manuscript id`() {
        val views = jdbcTemplate.queryForObject<Int>("SELECT views FROM manuscript WHERE id = 1")
        assertEquals(1, manuscriptRepository.incrementViews(1))
        assertEquals(views + 1, jdbcTemplate.queryForObject<Int>("SELECT views FROM manuscript WHERE id = 1"))
    }
    @Test fun `increment downloads by manuscript id`() {
        val downloads = jdbcTemplate.queryForObject<Int>("SELECT downloads FROM manuscript WHERE id = 1")
        assertEquals(1, manuscriptRepository.incrementDownloads(1))
        assertEquals(downloads + 1, jdbcTemplate.queryForObject<Int>("SELECT downloads FROM manuscript WHERE id = 1"))
    }
    @Test fun `retrieve publication id and section id of manuscript by manuscriptId`() {
        val (publicationId1, sectionId1) = manuscriptRepository.publicationIdAndSectionId(1)
        assertEquals(1, publicationId1)
        assertEquals(1, sectionId1)

        val (publicationId3, sectionId3) = manuscriptRepository.publicationIdAndSectionId(3)
        assertEquals(3, publicationId3)
        assertEquals(11, sectionId3)
    }
    @Test fun `insert manuscript`() {
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM manuscript WHERE title = 'new manuscript' AND author_id = 24 AND category_id = 1 AND section_id = 11 AND file_url = 'new file url')"))
        assertEquals(1, manuscriptRepository.insert("new manuscript", 24, 1, 11, "new file url"))
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM manuscript WHERE title = 'new manuscript' AND author_id = 24 AND category_id = 1 AND section_id = 11 AND file_url = 'new file url')"))
    }
    @Test fun `retrieve manuscripts pending review for account by account id where parent section and publication isn't hidden`() {
        assertEquals(
            listOf(
                // manuscript 1 is not present because it's section is hidden
                Manuscript(2, "Deep Learning in Genomics", "Analyzes genomic sequences using deep neural networks to predict mutations.", 11, 1, ManuscriptState.AWAITING_EDITOR_REVIEW, 7, "http://example.com/ms2.pdf", LocalDateTime.of(2023, 9, 28, 13, 28, 0), null, 245, 33),
                Manuscript(3, "Natural Language Processing in Clinical Notes", "Extracting insights from unstructured clinical data using NLP.", 10, 2, ManuscriptState.AWAITING_REVIEWER_REVIEW, 11, "http://example.com/ms3.pdf", LocalDateTime.of(2022, 9, 28, 13, 28, 0) , null, 310, 47),
                Manuscript(11, "Machine Learning for Radiology2", "A study on using ML to detect anomalies in radiological images.2", 10, 1, ManuscriptState.AWAITING_EIC_REVIEW, 2, "http://example.com/ms21.pdf", LocalDateTime.of(2016, 9, 28, 13, 28, 0), null, 135, 26)
            ),
            manuscriptRepository.pending(25)
        )
        assertEquals(
            listOf(
                Manuscript(2, "Deep Learning in Genomics", "Analyzes genomic sequences using deep neural networks to predict mutations.", 11, 1, ManuscriptState.AWAITING_EDITOR_REVIEW, 7, "http://example.com/ms2.pdf", LocalDateTime.of(2023, 9, 28, 13, 28, 0), null, 245, 33),
                Manuscript(3, "Natural Language Processing in Clinical Notes", "Extracting insights from unstructured clinical data using NLP.", 10, 2, ManuscriptState.AWAITING_REVIEWER_REVIEW, 11, "http://example.com/ms3.pdf", LocalDateTime.of(2022, 9, 28, 13, 28, 0) , null, 310, 47)
            ),
            manuscriptRepository.pending(26)
        )
        assertEquals(
            listOf(Manuscript(3, "Natural Language Processing in Clinical Notes", "Extracting insights from unstructured clinical data using NLP.", 10, 2, ManuscriptState.AWAITING_REVIEWER_REVIEW, 11, "http://example.com/ms3.pdf", LocalDateTime.of(2022, 9, 28, 13, 28, 0) , null, 310, 47)),
            manuscriptRepository.pending(27)
        )
    }
    @Test fun `retrieve manuscripts pending review for account by account id where parent section and publication isn't hidden by publication id`() {
        assertEquals(
            listOf(
                Manuscript(11, "Machine Learning for Radiology2", "A study on using ML to detect anomalies in radiological images.2", 10, 1, ManuscriptState.AWAITING_EIC_REVIEW, 2, "http://example.com/ms21.pdf", LocalDateTime.of(2016, 9, 28, 13, 28, 0), null, 135, 26)
            ),
            manuscriptRepository.pending(25, 1)
        )
        assertEquals(
            listOf(
                Manuscript(2, "Deep Learning in Genomics", "Analyzes genomic sequences using deep neural networks to predict mutations.", 11, 1, ManuscriptState.AWAITING_EDITOR_REVIEW, 7, "http://example.com/ms2.pdf", LocalDateTime.of(2023, 9, 28, 13, 28, 0), null, 245, 33),
            ),
            manuscriptRepository.pending(25, 2)
        )
        assertEquals(
            listOf(
                Manuscript(3, "Natural Language Processing in Clinical Notes", "Extracting insights from unstructured clinical data using NLP.", 10, 2, ManuscriptState.AWAITING_REVIEWER_REVIEW, 11, "http://example.com/ms3.pdf", LocalDateTime.of(2022, 9, 28, 13, 28, 0) , null, 310, 47),
            ),
            manuscriptRepository.pending(25, 3)
        )
        assertEquals(listOf(), manuscriptRepository.pending(26, 1))
        assertEquals(listOf(), manuscriptRepository.pending(27, 1))
        assertEquals(listOf(), manuscriptRepository.pending(27, 2))
    }
    @Test fun `check if manuscript exists by id`() {
        assertTrue(manuscriptRepository.exists(1))
        assertFalse(manuscriptRepository.exists(1000))
    }
    @Test fun `update manuscript state`() {
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM manuscript WHERE id = 1 AND current_state = 'AWAITING_EIC_REVIEW')"))
        manuscriptRepository.updateState(1, ManuscriptState.AWAITING_EDITOR_REVIEW)
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM manuscript WHERE id = 1 AND current_state = 'AWAITING_EDITOR_REVIEW')"))
    }
    @Test fun `delete manuscript`() {
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM manuscript WHERE id = 21 AND title = 'Manuscript awaiting deletion')"))
        manuscriptRepository.delete(21)
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM manuscript WHERE id = 21 AND title = 'Manuscript awaiting deletion')"))
    }
    @Test fun `retrieve manuscript by id`() {
        assertEquals(
            Manuscript(2, "Deep Learning in Genomics", "Analyzes genomic sequences using deep neural networks to predict mutations.", 11, 1, ManuscriptState.AWAITING_EDITOR_REVIEW, 7, "http://example.com/ms2.pdf", LocalDateTime.of(2023, 9, 28, 13, 28, 0), null, 245, 33),
            manuscriptRepository.byId(2)
        )
    }
    @Test fun `retrieve all manuscripts contained in certain section by section id`() {
        assertEquals(
            listOf(
                Manuscript(21, "Manuscript awaiting deletion", "description of manuscript awaiting deletion", 11, 5, ManuscriptState.REJECTED, 2, "http://example.com/ms210.pdf", LocalDateTime.of(2007, 9, 28, 13, 28, 0), null, 76, 15),
                Manuscript(20, "Wearable Technology and AI Integration2", "Leveraging real-time health data from wearables through AI.2", 11, 5, ManuscriptState.DRAFT, 2, "http://example.com/ms210.pdf", LocalDateTime.of(2007, 9, 28, 13, 28, 0), null, 76, 15),
                Manuscript(19, "Clinical Trial Optimization with AI2", "Optimizing patient recruitment and trial design using machine learning.2", 10, 5, ManuscriptState.HIDDEN, 2, "http://example.com/ms29.pdf", LocalDateTime.of(2008, 9, 28, 13, 28, 0), LocalDateTime.of(2025, 7, 25, 17, 45, 0), 61, 8),
                Manuscript(18, "AI-Based Mental Health Assessment2", "Assessing mental health using sentiment analysis and behavioral data.2", 11, 4, ManuscriptState.ARCHIVED, 2, "http://example.com/ms28.pdf", LocalDateTime.of(2009, 9, 28, 13, 28, 0), LocalDateTime.of(2025, 6, 30, 13, 30, 0), 22, 4),
                Manuscript(17, "Predictive Analytics in Emergency Care2", "Forecasting patient outcomes in emergency rooms using AI.2", 10, 4, ManuscriptState.REJECTED, 2, "http://example.com/ms27.pdf", LocalDateTime.of(2010, 9, 28, 13, 28, 0), null, 46, 6),
                Manuscript(16, "AI in Medical Decision Making2", "Examining the implications of autonomous decision systems in healthcare.2", 11, 3, ManuscriptState.PUBLISHED, 2, "http://example.com/ms26.pdf", LocalDateTime.of(2011, 9, 28, 13, 28, 0), LocalDateTime.of(2025, 6, 30, 13, 32, 16), 513, 89),
                Manuscript(15, "Reinforcement Learning in Surgery2", "Simulating surgical procedures using reinforcement learning algorithms.2", 10, 3, ManuscriptState.MAJOR, 2, "http://example.com/ms25.pdf", LocalDateTime.of(2012, 9, 28, 13, 28, 0), null, 88, 13),
                Manuscript(14, "AI-Powered Drug Discovery2", "Accelerating pharmaceutical research through predictive modeling.2", 11, 2, ManuscriptState.MINOR, 2, "http://example.com/ms24.pdf", LocalDateTime.of(2013, 9, 28, 13, 28, 0), null, 99, 20),
                Manuscript(13, "Natural Language Processing in Clinical Notes2", "Extracting insights from unstructured clinical data using NLP.2", 10, 2, ManuscriptState.AWAITING_REVIEWER_REVIEW, 2, "http://example.com/ms23.pdf", LocalDateTime.of(2014, 9, 28, 13, 28, 0), null, 311, 48),
                Manuscript(12, "Deep Learning in Genomics2", "Analyzes genomic sequences using deep neural networks to predict mutations.2", 11, 1, ManuscriptState.AWAITING_EDITOR_REVIEW, 2, "http://example.com/ms22.pdf", LocalDateTime.of(2015, 9, 28, 13, 28, 0), null, 246, 34),
                Manuscript(11, "Machine Learning for Radiology2", "A study on using ML to detect anomalies in radiological images.2", 10, 1, ManuscriptState.AWAITING_EIC_REVIEW, 2, "http://example.com/ms21.pdf", LocalDateTime.of(2016, 9, 28, 13, 28, 0), null, 135, 26)
            ),
            manuscriptRepository.allBySectionId(sectionId = 2)
        )
    }
    @Test fun `retrieve all manuscripts contained in certain section by section id and manuscript state`() {
        assertEquals(
            listOf(
                Manuscript(16, "AI in Medical Decision Making2", "Examining the implications of autonomous decision systems in healthcare.2", 11, 3, ManuscriptState.PUBLISHED, 2, "http://example.com/ms26.pdf", LocalDateTime.of(2011, 9, 28, 13, 28, 0), LocalDateTime.of(2025, 6, 30, 13, 32, 16), 513, 89),
            ),
            manuscriptRepository.allBySectionId(sectionId = 2, ManuscriptState.PUBLISHED)
        )
    }
    @Test fun `retrieve all manuscripts by author id`() {
        assertEquals(
            listOf(
                Manuscript(19, "Clinical Trial Optimization with AI2", "Optimizing patient recruitment and trial design using machine learning.2", 10, 5, ManuscriptState.HIDDEN, 2, "http://example.com/ms29.pdf", LocalDateTime.of(2008, 9, 28, 13, 28, 0), LocalDateTime.of(2025, 7, 25, 17, 45, 0), 61, 8),
                Manuscript(17, "Predictive Analytics in Emergency Care2", "Forecasting patient outcomes in emergency rooms using AI.2", 10, 4, ManuscriptState.REJECTED, 2, "http://example.com/ms27.pdf", LocalDateTime.of(2010, 9, 28, 13, 28, 0), null, 46, 6),
                Manuscript(15, "Reinforcement Learning in Surgery2", "Simulating surgical procedures using reinforcement learning algorithms.2", 10, 3, ManuscriptState.MAJOR, 2, "http://example.com/ms25.pdf", LocalDateTime.of(2012, 9, 28, 13, 28, 0), null, 88, 13),
                Manuscript(13, "Natural Language Processing in Clinical Notes2", "Extracting insights from unstructured clinical data using NLP.2", 10, 2, ManuscriptState.AWAITING_REVIEWER_REVIEW, 2, "http://example.com/ms23.pdf", LocalDateTime.of(2014, 9, 28, 13, 28, 0), null, 311, 48),
                Manuscript(11, "Machine Learning for Radiology2", "A study on using ML to detect anomalies in radiological images.2", 10, 1, ManuscriptState.AWAITING_EIC_REVIEW, 2, "http://example.com/ms21.pdf", LocalDateTime.of(2016, 9, 28, 13, 28, 0), null, 135, 26),
                Manuscript(9, "Clinical Trial Optimization with AI", "Optimizing patient recruitment and trial design using machine learning.", 10, 5, ManuscriptState.HIDDEN, 1, "http://example.com/ms9.pdf", LocalDateTime.of(2018, 9, 28, 13, 28, 0), LocalDateTime.of(2025, 7, 25, 17, 45, 0), 60, 7),
                Manuscript(7, "Predictive Analytics in Emergency Care", "Forecasting patient outcomes in emergency rooms using AI.", 10, 4, ManuscriptState.REJECTED, 1, "http://example.com/ms7.pdf", LocalDateTime.of(2025, 9, 28, 13, 28, 0), null, 45, 5),
                Manuscript(5, "Reinforcement Learning in Surgery", "Simulating surgical procedures using reinforcement learning algorithms.", 10, 3, ManuscriptState.MAJOR, 1, "http://example.com/ms5.pdf", LocalDateTime.of(2021, 9, 28, 13, 28, 0), null, 87, 12),
                Manuscript(3, "Natural Language Processing in Clinical Notes", "Extracting insights from unstructured clinical data using NLP.", 10, 2, ManuscriptState.AWAITING_REVIEWER_REVIEW, 11, "http://example.com/ms3.pdf", LocalDateTime.of(2022, 9, 28, 13, 28, 0), null, 310, 47),
                Manuscript(1, "Machine Learning for Radiology", "A study on using ML to detect anomalies in radiological images.", 10, 1, ManuscriptState.AWAITING_EIC_REVIEW, 1, "http://example.com/ms1.pdf", LocalDateTime.of(2025, 9, 28, 13, 28, 0), null, 134, 25)
            ),
            manuscriptRepository.allByAuthorId(authorId = 10)
        )
    }
    @Test fun `retrieve all manuscripts by author id and manuscript state`() {
        assertEquals(
            listOf(
                Manuscript(13, "Natural Language Processing in Clinical Notes2", "Extracting insights from unstructured clinical data using NLP.2", 10, 2, ManuscriptState.AWAITING_REVIEWER_REVIEW, 2, "http://example.com/ms23.pdf", LocalDateTime.of(2014, 9, 28, 13, 28, 0), null, 311, 48),
                Manuscript(3, "Natural Language Processing in Clinical Notes", "Extracting insights from unstructured clinical data using NLP.", 10, 2, ManuscriptState.AWAITING_REVIEWER_REVIEW, 11, "http://example.com/ms3.pdf", LocalDateTime.of(2022, 9, 28, 13, 28, 0), null, 310, 47),
            ),
            manuscriptRepository.allByAuthorId(authorId = 10, ManuscriptState.AWAITING_REVIEWER_REVIEW)
        )
    }
}