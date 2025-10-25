package hr.unipu.journals.feature.publication.core

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

    @Test
    fun `retrieve all publications which contain pending manuscripts by account id`() {
        val publications = listOf(
            Publication(1, "Journal of AI Research", false),
            Publication(2, "Nature of Biology", false),
            Publication(3, "Physics Letters", false),
        )
        assertEquals(publications, publicationRepository.allWithPendingManuscripts(accountId = 25))
        assertEquals(publications.takeLast(2), publicationRepository.allWithPendingManuscripts(accountId = 26))
        assertEquals(publications.takeLast(1), publicationRepository.allWithPendingManuscripts(accountId = 27))
    }
    @Test
    fun `retrieve publication title by publication id`() {
        assertEquals("Journal of AI Research", publicationRepository.title(1))
    }
    @Test
    fun `retrieve all published publications`() {
        assertEquals(
            listOf(
                Publication(6, "Published empty publication", false),
                Publication(3, "Physics Letters", false),
                Publication(2, "Nature of Biology", false),
                Publication(1, "Journal of AI Research", false),
            ),
            publicationRepository.allPublished()
        )
    }
    @Test
    fun `retrieve all publications that are hidden or contain hidden sections or hidden manuscripts`() {
        assertEquals(
            listOf(
                Publication(1, "Journal of AI Research", false),
                Publication(2, "Nature of Biology", false),
                Publication(3, "Physics Letters", false),
                Publication(4, "first hidden publication", true),
                Publication(5, "second hidden publication", true),
            ),
            publicationRepository.allHidden()
        )
    }
    @Test
    fun `retrieve all publications containing archived manuscripts`() {
        assertEquals(
            listOf(Publication(1, "Journal of AI Research", false)),
            publicationRepository.allContainingArchivedManuscripts()
        )
    }
    @Test
    fun `insert publication`() {
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE title = 'new publication')"))
        assertEquals(1, publicationRepository.insert("new publication"))
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE title = 'new publication')"))
    }
    @Test
    fun `update publication title`() {
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE title = 'updated publication title')"))
        assertEquals(1, publicationRepository.updateTitle(1, "updated publication title"))
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE title = 'updated publication title')"))
    }
    @Test
    fun `check if publication exists`() {
        Assertions.assertTrue(publicationRepository.exists(1))
        Assertions.assertFalse(publicationRepository.exists(1000))
    }
    @Test
    fun `update is publication hidden`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE id = 2 AND is_hidden = FALSE)"))
        assertEquals(1, publicationRepository.updateHidden(2, true))
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE id = 2 AND is_hidden = TRUE)"))
    }
    @Test
    fun `delete publication by publication id`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE id = 1)"))
        assertEquals(1, publicationRepository.delete(1))
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM publication WHERE id = 1)"))
    }
}