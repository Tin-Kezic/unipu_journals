package hr.unipu.journals.feature.publication.eic_on_publication

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface EicOnPublicationRepository: Repository<EicOnPublication, Int> {

    @Query("SELECT EXISTS (SELECT 1 FROM eic_on_publication WHERE publication_id = :publication_id AND eic_id = :eic_id)")
    fun isEicOnPublication(@Param("eic_id") eicId: Int, @Param("publication_id") publicationId: Int): Boolean

    @Query("""
        SELECT account.email FROM eic_on_publication
        JOIN account ON eic_on_publication.eic_id = account.id
        WHERE eic_on_publication.publication_id = :publication_id
    """)
    fun eicEmailsByPublicationId(@Param("publication_id") publicationId: Int): List<String>

    @Query("SELECT publication_id FROM eic_on_publication WHERE eic_id = :eic_id")
    fun allAffiliatedPublicationIds(@Param("eic_id") eicId: Int): List<Int>

    @Modifying
    @Query("INSERT INTO eic_on_publication (publication_id, eic_id) VALUES (:publication_id, :eic_id)")
    fun assign(@Param("eic_id") eicId: Int, @Param("publication_id") publicationId: Int): Int

    @Modifying
    @Query("DELETE FROM eic_on_publication WHERE eic_id = :eic_id AND (publication_id = :publication_id OR :publication_id IS NULL)")
    fun revoke(@Param("eic_id") eicId: Int, @Param("publication_id") publicationId: Int? = null): Int
}