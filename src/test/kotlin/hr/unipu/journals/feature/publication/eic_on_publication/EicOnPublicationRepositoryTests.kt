package hr.unipu.journals.feature.publication.eic_on_publication

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject

@DataJdbcTest
class EicOnPublicationRepositoryTests {
    @Autowired private lateinit var eicOnPublicationRepository: EicOnPublicationRepository
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @Test fun `assert is eic on publication`() {
        Assertions.assertTrue(eicOnPublicationRepository.isEicOnPublication(7, 1))
        Assertions.assertTrue(eicOnPublicationRepository.isEicOnPublication(6, 2))
        Assertions.assertTrue(eicOnPublicationRepository.isEicOnPublication(7, 3))
        Assertions.assertTrue(eicOnPublicationRepository.isEicOnPublication(6, 4))
        Assertions.assertTrue(eicOnPublicationRepository.isEicOnPublication(7, 5))

        Assertions.assertFalse(eicOnPublicationRepository.isEicOnPublication(6, 1))
        Assertions.assertFalse(eicOnPublicationRepository.isEicOnPublication(7, 2))
        Assertions.assertFalse(eicOnPublicationRepository.isEicOnPublication(6, 3))
        Assertions.assertFalse(eicOnPublicationRepository.isEicOnPublication(7, 4))
        Assertions.assertFalse(eicOnPublicationRepository.isEicOnPublication(7, 6))
        Assertions.assertFalse(eicOnPublicationRepository.isEicOnPublication(2, 7))
        Assertions.assertFalse(eicOnPublicationRepository.isEicOnPublication(2, 10))
        Assertions.assertFalse(eicOnPublicationRepository.isEicOnPublication(1, 9))
        Assertions.assertFalse(eicOnPublicationRepository.isEicOnPublication(3, 8))
        Assertions.assertFalse(eicOnPublicationRepository.isEicOnPublication(6, 5))
    }
    @Test fun `retrieve eic emails by publication id`() {
        assertEquals(listOf("eic2.on.publication@unipu.hr"), eicOnPublicationRepository.eicEmailsByPublicationId(1))
        assertEquals(listOf("eic1.on.publication@unipu.hr"), eicOnPublicationRepository.eicEmailsByPublicationId(2))
        assertEquals(listOf("eic2.on.publication@unipu.hr"), eicOnPublicationRepository.eicEmailsByPublicationId(3))
        assertEquals(listOf("eic1.on.publication@unipu.hr"), eicOnPublicationRepository.eicEmailsByPublicationId(4))
        assertEquals(listOf("eic2.on.publication@unipu.hr"), eicOnPublicationRepository.eicEmailsByPublicationId(5))
    }
    @Test fun `assign eic on publication`() {
        eicOnPublicationRepository.assign(2, 8)
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM eic_on_publication WHERE publication_id = 2 AND eic_id = 8)"))
    }
    @Test fun `revoke eic on publication`() {
        Assertions.assertTrue(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM eic_on_publication WHERE publication_id = 3 AND eic_id = 7)"))
        eicOnPublicationRepository.revoke(3, 7)
        Assertions.assertFalse(jdbcTemplate.queryForObject<Boolean>("SELECT EXISTS (SELECT 1 FROM eic_on_publication WHERE publication_id = 3 AND eic_id = 7)"))
    }
}