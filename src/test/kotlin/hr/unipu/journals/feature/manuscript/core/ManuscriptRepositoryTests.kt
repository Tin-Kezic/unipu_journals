package hr.unipu.journals.feature.manuscript.core

import hr.unipu.journals.feature.publication.core.Affiliation
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import java.time.LocalDateTime

@DataJdbcTest
class ManuscriptRepositoryTests {
    @Autowired private lateinit var manuscriptRepository: ManuscriptRepository
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @Test fun `increment views by manuscript id`() {
        val views = jdbcTemplate.queryForObject<Int>("SELECT views FROM manuscript WHERE id = 1")
        assertEquals(1, manuscriptRepository.incrementViews(1))
        assertEquals(views + 1, jdbcTemplate.queryForObject<Int>("SELECT views FROM manuscript WHERE id = 1"))
    }
    @Test fun `insert manuscript`() {
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM manuscript WHERE title = 'new manuscript' AND category_id = 1 AND section_id = 11 AND file_url = 'new file url')"))
        assertEquals(1, manuscriptRepository.insert("new manuscript", 1, 11, "new file url"))
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM manuscript WHERE title = 'new manuscript'  AND category_id = 1 AND section_id = 11 AND file_url = 'new file url')"))
    }
    @Test fun `retrieve manuscripts pending review for account by account id where parent section and publication isn't hidden`() {
        assertEquals(
            listOf(
                // manuscript 1 is not present because it's section is hidden
                Manuscript(2, "Deep Learning in Genomics", "Analyzes genomic sequences using deep neural networks to predict mutations.", 1, ManuscriptState.AWAITING_EDITOR_REVIEW, 7, "http://example.com/ms2.pdf", LocalDateTime.of(2023, 9, 28, 13, 28, 0), null, 245),
                Manuscript(11, "Machine Learning for Radiology2", "A study on using ML to detect anomalies in radiological images.2", 1, ManuscriptState.AWAITING_EIC_REVIEW, 2, "http://example.com/ms21.pdf", LocalDateTime.of(2016, 9, 28, 13, 28, 0), null, 135),
                Manuscript(3, "Natural Language Processing in Clinical Notes", "Extracting insights from unstructured clinical data using NLP.", 2, ManuscriptState.AWAITING_REVIEWER_REVIEW, 11, "http://example.com/ms3.pdf", LocalDateTime.of(2022, 9, 28, 13, 28, 0), null, 310),
            ),
            manuscriptRepository.all(manuscriptStateFilter = ManuscriptStateFilter.ALL_AWAITING_REVIEW, accountId = 25)
        )
        assertEquals(
            listOf(
                Manuscript(2, "Deep Learning in Genomics", "Analyzes genomic sequences using deep neural networks to predict mutations.", 1, ManuscriptState.AWAITING_EDITOR_REVIEW, 7, "http://example.com/ms2.pdf", LocalDateTime.of(2023, 9, 28, 13, 28, 0), null, 245),
                Manuscript(3, "Natural Language Processing in Clinical Notes", "Extracting insights from unstructured clinical data using NLP.", 2, ManuscriptState.AWAITING_REVIEWER_REVIEW, 11, "http://example.com/ms3.pdf", LocalDateTime.of(2022, 9, 28, 13, 28, 0), null, 310)
            ),
            manuscriptRepository.all(manuscriptStateFilter = ManuscriptStateFilter.ALL_AWAITING_REVIEW, accountId = 26)
        )
        assertEquals(
            listOf(
                Manuscript(3, "Natural Language Processing in Clinical Notes", "Extracting insights from unstructured clinical data using NLP.", 2, ManuscriptState.AWAITING_REVIEWER_REVIEW, 11, "http://example.com/ms3.pdf", LocalDateTime.of(2022, 9, 28, 13, 28, 0), null, 310)
            ),
            manuscriptRepository.all(manuscriptStateFilter = ManuscriptStateFilter.ALL_AWAITING_REVIEW, accountId = 27)
        )
    }
    @Test
    fun `check if manuscript exists by id`() {
        Assertions.assertTrue(manuscriptRepository.exists(1))
        Assertions.assertFalse(manuscriptRepository.exists(1000))
    }
    @Test
    fun `update manuscript state`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM manuscript WHERE id = 1 AND current_state = 'AWAITING_EIC_REVIEW')"))
        manuscriptRepository.updateState(1, ManuscriptState.AWAITING_EDITOR_REVIEW)
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM manuscript WHERE id = 1 AND current_state = 'AWAITING_EDITOR_REVIEW')"))
    }
    @Test
    fun `delete manuscript`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM manuscript WHERE id = 21 AND title = 'Manuscript awaiting deletion')"))
        manuscriptRepository.delete(21)
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM manuscript WHERE id = 21 AND title = 'Manuscript awaiting deletion')"))
    }
    @Test
    fun `retrieve manuscript by id`() {
        assertEquals(
            Manuscript(2, "Deep Learning in Genomics", "Analyzes genomic sequences using deep neural networks to predict mutations.", 1, ManuscriptState.AWAITING_EDITOR_REVIEW, 7, "http://example.com/ms2.pdf", LocalDateTime.of(2023, 9, 28, 13, 28, 0), null, 245),
            manuscriptRepository.byId(2)
        )
    }
    @Test
    fun `retrieve all manuscripts contained in certain section by section id`() {
        assertEquals(
            listOf(
                Manuscript(16, "AI in Medical Decision Making2", "Examining the implications of autonomous decision systems in healthcare.2", 3, ManuscriptState.PUBLISHED, 2, "http://example.com/ms26.pdf", LocalDateTime.of(2011, 9, 28, 13, 28, 0), LocalDateTime.of(2025, 6, 30, 13, 32, 16), 513),
                Manuscript(18, "AI-Based Mental Health Assessment2", "Assessing mental health using sentiment analysis and behavioral data.2", 4, ManuscriptState.ARCHIVED, 2, "http://example.com/ms28.pdf", LocalDateTime.of(2009, 9, 28, 13, 28, 0), LocalDateTime.of(2025, 6, 30, 13, 30, 0), 22),
                Manuscript(14, "AI-Powered Drug Discovery2", "Accelerating pharmaceutical research through predictive modeling.2", 2, ManuscriptState.MINOR, 2, "http://example.com/ms24.pdf", LocalDateTime.of(2013, 9, 28, 13, 28, 0), null, 99),
                Manuscript(19, "Clinical Trial Optimization with AI2", "Optimizing patient recruitment and trial design using machine learning.2", 5, ManuscriptState.HIDDEN, 2, "http://example.com/ms29.pdf", LocalDateTime.of(2008, 9, 28, 13, 28, 0), LocalDateTime.of(2025, 7, 25, 17, 45, 0), 61),
                Manuscript(12, "Deep Learning in Genomics2", "Analyzes genomic sequences using deep neural networks to predict mutations.2", 1, ManuscriptState.AWAITING_EDITOR_REVIEW, 2, "http://example.com/ms22.pdf", LocalDateTime.of(2015, 9, 28, 13, 28, 0), null, 246),
                Manuscript(11, "Machine Learning for Radiology2", "A study on using ML to detect anomalies in radiological images.2", 1, ManuscriptState.AWAITING_EIC_REVIEW, 2, "http://example.com/ms21.pdf", LocalDateTime.of(2016, 9, 28, 13, 28, 0), null, 135),
                Manuscript(21, "Manuscript awaiting deletion", "description of manuscript awaiting deletion", 5, ManuscriptState.REJECTED, 2, "http://example.com/ms210.pdf", LocalDateTime.of(2007, 9, 28, 13, 28, 0), null, 76),
                Manuscript(13, "Natural Language Processing in Clinical Notes2", "Extracting insights from unstructured clinical data using NLP.2", 2, ManuscriptState.AWAITING_REVIEWER_REVIEW, 2, "http://example.com/ms23.pdf", LocalDateTime.of(2014, 9, 28, 13, 28, 0), null, 348),
                Manuscript(17, "Predictive Analytics in Emergency Care2", "Forecasting patient outcomes in emergency rooms using AI.2", 4, ManuscriptState.REJECTED, 2, "http://example.com/ms27.pdf", LocalDateTime.of(2010, 9, 28, 13, 28, 0), null, 46),
                Manuscript(15, "Reinforcement Learning in Surgery2", "Simulating surgical procedures using reinforcement learning algorithms.2", 3, ManuscriptState.MAJOR, 2, "http://example.com/ms25.pdf", LocalDateTime.of(2012, 9, 28, 13, 28, 0), null, 88),
                Manuscript(20, "Wearable Technology and AI Integration2", "Leveraging real-time health data from wearables through AI.2", 5, ManuscriptState.AWAITING_REVIEWER_REVIEW, 2, "http://example.com/ms210.pdf", LocalDateTime.of(2007, 9, 28, 13, 28, 0), null, 76),
            ),
            manuscriptRepository.all(sectionId = 2)
        )
    }
    @Test
    fun `retrieve all manuscripts contained in certain section by section id and manuscript state`() {
        assertEquals(
            listOf(
                Manuscript(16, "AI in Medical Decision Making2", "Examining the implications of autonomous decision systems in healthcare.2", 3, ManuscriptState.PUBLISHED, 2, "http://example.com/ms26.pdf", LocalDateTime.of(2011, 9, 28, 13, 28, 0), LocalDateTime.of(2025, 6, 30, 13, 32, 16), 513),
            ),
            manuscriptRepository.all(sectionId = 2, manuscriptStateFilter = ManuscriptStateFilter.PUBLISHED)
        )
    }
    @Test
    fun `retrieve all manuscripts by author id`() {
        assertEquals(
            listOf(Manuscript(1, "Machine Learning for Radiology", "A study on using ML to detect anomalies in radiological images.", 1, ManuscriptState.AWAITING_EIC_REVIEW, 1, "http://example.com/ms1.pdf", LocalDateTime.of(2025, 9, 28, 13, 28, 0), null, 134)),
            manuscriptRepository.all(accountId = 19, affiliation = Affiliation.AUTHOR)
        )
    }
    @Test
    fun `retrieve all manuscripts by author id and manuscript state`() {
        assertEquals(
            listOf(
                Manuscript(2, "Deep Learning in Genomics", "Analyzes genomic sequences using deep neural networks to predict mutations.", 1, ManuscriptState.AWAITING_EDITOR_REVIEW, 7, "http://example.com/ms2.pdf", LocalDateTime.of(2023, 9, 28, 13, 28, 0), null, 245),
                Manuscript(11, "Machine Learning for Radiology2", "A study on using ML to detect anomalies in radiological images.2", 1, ManuscriptState.AWAITING_EIC_REVIEW, 2, "http://example.com/ms21.pdf", LocalDateTime.of(2016, 9, 28, 13, 28, 0), null, 135),
                Manuscript(3, "Natural Language Processing in Clinical Notes", "Extracting insights from unstructured clinical data using NLP.", 2, ManuscriptState.AWAITING_REVIEWER_REVIEW, 11, "http://example.com/ms3.pdf", LocalDateTime.of(2022, 9, 28, 13, 28, 0) , null, 310),
            ),
            manuscriptRepository.all(manuscriptStateFilter = ManuscriptStateFilter.ALL_AWAITING_REVIEW, accountId = 25)
        )
    }
}