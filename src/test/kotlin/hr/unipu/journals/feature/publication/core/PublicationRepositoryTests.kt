package hr.unipu.journals.feature.publication.core

import hr.unipu.journals.feature.manuscript.core.ManuscriptStateFilter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject

@DataJdbcTest
class PublicationRepositoryTests {
    @Autowired private lateinit var publicationRepository: PublicationRepository
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @Test fun `retrieve all publications which contain pending manuscripts by account id`() {
        val publications = listOf(
            Publication(1, "Journal of AI Research", false),
            Publication(2, "Nature of Biology", false),
            Publication(3, "Physics Letters", false),
        )
        assertEquals(publications, publicationRepository.all(ManuscriptStateFilter.ALL_AWAITING_REVIEW, accountId = 25))
        assertEquals(publications.takeLast(2), publicationRepository.all(ManuscriptStateFilter.ALL_AWAITING_REVIEW, accountId = 26))
        assertEquals(publications.takeLast(1), publicationRepository.all(ManuscriptStateFilter.ALL_AWAITING_REVIEW, accountId = 27))
    }
    @Test fun `retrieve all public publications with manuscript state`() {
        assertEquals(
            listOf(Publication(1, "Journal of AI Research", false)),
            publicationRepository.all(ManuscriptStateFilter.ARCHIVED)
        )
    }
    @Test fun `retrieve all public publications with category`() {
        assertEquals(
            listOf(
                Publication(1, "Journal of AI Research", false),
                Publication(2, "Nature of Biology", false),
            ),
            publicationRepository.all(ManuscriptStateFilter.PUBLISHED, category = "Computer Science")
        )
    }
    @Test fun `retrieve all public publications with affiliation`() {
        assertEquals(
            listOf(Publication(1, "Journal of AI Research", false), Publication(3, "Physics Letters", false)),
            publicationRepository.all(ManuscriptStateFilter.PUBLISHED, Role.EIC_ON_PUBLICATION, 7)
        )
        assertEquals(
            listOf(Publication(2, "Nature of Biology", false)),
            publicationRepository.all(ManuscriptStateFilter.AWAITING_EDITOR_REVIEW, Role.EDITOR, 14)
        )
    }
    @Test fun `retrieve publication by publication id`() {
        assertEquals("Journal of AI Research", publicationRepository.by(id = 1)?.title)
    }
    @Test fun `retrieve all published publications`() {
        assertEquals(
            listOf(
                Publication(1, "Journal of AI Research", false),
                Publication(2, "Nature of Biology", false),
                Publication(3, "Physics Letters", false),
            ),
            publicationRepository.all(ManuscriptStateFilter.PUBLISHED)
        )
        assertEquals(
            listOf(
                Publication(1, "Journal of AI Research", false),
                Publication(2, "Nature of Biology", false),
                Publication(3, "Physics Letters", false),
                Publication(6, "Published empty publication", false),
            ),
            publicationRepository.all(ManuscriptStateFilter.PUBLISHED, accountId = 2)
        )
    }
    @Test fun `retrieve all publications that are hidden or contain hidden sections or hidden manuscripts`() {
        assertEquals(
            listOf(
                Publication(1, "Journal of AI Research", false),
                Publication(2, "Nature of Biology", false),
                Publication(3, "Physics Letters", false),
                Publication(4, "first hidden publication", true),
                Publication(5, "second hidden publication", true),
            ),
            publicationRepository.all(ManuscriptStateFilter.HIDDEN)
        )
    }
    @Test fun `retrieve all publications containing archived manuscripts`() {
        assertEquals(
            listOf(Publication(1, "Journal of AI Research", false)),
            publicationRepository.all(ManuscriptStateFilter.ARCHIVED)
        )
    }
    @Test fun `insert publication`() {
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE title = 'new publication')"))
        assertEquals(1, publicationRepository.insert("new publication"))
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE title = 'new publication')"))
    }
    @Test fun `update publication title`() {
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE title = 'updated publication title')"))
        assertEquals(1, publicationRepository.update(1, title = "updated publication title"))
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE title = 'updated publication title')"))
    }
    @Test fun `check if publication exists`() {
        Assertions.assertTrue(publicationRepository.exists(1))
        Assertions.assertFalse(publicationRepository.exists(1000))
    }
    @Test fun `update is publication hidden`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE id = 2 AND is_hidden = FALSE)"))
        assertEquals(1, publicationRepository.update(2, isHidden = true))
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE id = 2 AND is_hidden = TRUE)"))
    }
    @Test fun `delete publication by publication id`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE id = 1)"))
        assertEquals(1, publicationRepository.delete(1))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE id = 1)"))
    }
}