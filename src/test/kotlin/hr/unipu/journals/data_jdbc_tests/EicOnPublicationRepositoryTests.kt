package hr.unipu.journals.data_jdbc_tests

import hr.unipu.journals.feature.eic_on_publication.EicOnPublicationRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import kotlin.test.Test
import kotlin.test.assertContains

@DataJdbcTest
class EicOnPublicationRepositoryTests {
    @Autowired private lateinit var eicOnPublicationRepository: EicOnPublicationRepository
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @Test fun `is eic on publication`() {
        assertTrue(eicOnPublicationRepository.isEicOnPublication(7, 1))
        assertTrue(eicOnPublicationRepository.isEicOnPublication(6, 2))
        assertTrue(eicOnPublicationRepository.isEicOnPublication(7, 3))
        assertTrue(eicOnPublicationRepository.isEicOnPublication(6, 4))
        assertTrue(eicOnPublicationRepository.isEicOnPublication(7, 5))

        assertFalse(eicOnPublicationRepository.isEicOnPublication(6, 1))
        assertFalse(eicOnPublicationRepository.isEicOnPublication(7, 2))
        assertFalse(eicOnPublicationRepository.isEicOnPublication(6, 3))
        assertFalse(eicOnPublicationRepository.isEicOnPublication(7, 4))
        assertFalse(eicOnPublicationRepository.isEicOnPublication(7, 6))
        assertFalse(eicOnPublicationRepository.isEicOnPublication(2, 7))
        assertFalse(eicOnPublicationRepository.isEicOnPublication(2, 10))
        assertFalse(eicOnPublicationRepository.isEicOnPublication(1, 9))
        assertFalse(eicOnPublicationRepository.isEicOnPublication(3, 8))
        assertFalse(eicOnPublicationRepository.isEicOnPublication(6, 5))
    }
    @Test fun `retrieve eic emails by publication id`() {
        assertContains(eicOnPublicationRepository.eicEmailsByPublicationId(1), "eic2.on.publication@unipu.hr")
        assertContains(eicOnPublicationRepository.eicEmailsByPublicationId(2), "eic1.on.publication@unipu.hr")
        assertContains(eicOnPublicationRepository.eicEmailsByPublicationId(3), "eic2.on.publication@unipu.hr")
        assertContains(eicOnPublicationRepository.eicEmailsByPublicationId(4), "eic1.on.publication@unipu.hr")
        assertContains(eicOnPublicationRepository.eicEmailsByPublicationId(5), "eic2.on.publication@unipu.hr")

        assertFalse(eicOnPublicationRepository.eicEmailsByPublicationId(1).contains("eic1.on.publication@unipu.hr"))
        assertFalse(eicOnPublicationRepository.eicEmailsByPublicationId(2).contains("eic2.on.publication@unipu.hr"))
        assertFalse(eicOnPublicationRepository.eicEmailsByPublicationId(3).contains("eic1.on.publication@unipu.hr"))
        assertFalse(eicOnPublicationRepository.eicEmailsByPublicationId(4).contains("eic2.on.publication@unipu.hr"))
        assertFalse(eicOnPublicationRepository.eicEmailsByPublicationId(5).contains("eic1.on.publication@unipu.hr"))
    }
    @Test fun `assign eic on publication`() {
        eicOnPublicationRepository.assign(2, 8)
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM eic_on_publication WHERE publication_id = 2 AND eic_id = 8)"))
    }
    @Test fun `revoke eic on publication`() {
        assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM eic_on_publication WHERE publication_id = 3 AND eic_id = 7)"))
        eicOnPublicationRepository.revoke(3, 7)
        assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM eic_on_publication WHERE publication_id = 3 AND eic_id = 7)"))
    }
}