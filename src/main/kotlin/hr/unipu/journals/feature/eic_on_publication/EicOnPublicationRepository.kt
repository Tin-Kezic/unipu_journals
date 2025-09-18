package hr.unipu.journals.feature.eic_on_publication

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val EIC_ON_PUBLICATION = "eic_on_publication"
private const val ID = "id"
private const val PUBLICATION_ID = "publication_id"
private const val EIC_ID = "eic_id"

// account
private const val ACCOUNT = "account"
//private const val ID = "id"
private const val FULL_NAME = "full_name"
private const val TITLE = "title"
private const val EMAIL = "email"
private const val PASSWORD = "password"
private const val AFFILIATION = "affiliation"
private const val JOB_TYPE = "job_type"
private const val COUNTRY = "country"
private const val CITY = "city"
private const val ADDRESS = "address"
private const val ZIP_CODE = "zip_code"
private const val IS_ADMIN = "is_admin"

interface EicOnPublicationRepository: Repository<EicOnPublication, Int> {

    @Query("SELECT EXISTS (SELECT 1 FROM $EIC_ON_PUBLICATION WHERE $PUBLICATION_ID = :$PUBLICATION_ID AND $EIC_ID = :$EIC_ID)")
    fun isEicOnPublication(@Param(EIC_ID) eicId: Int, @Param(PUBLICATION_ID) publicationId: Int): Boolean

    @Query("""
        SELECT $ACCOUNT.$EMAIL FROM $EIC_ON_PUBLICATION
        JOIN $ACCOUNT ON $EIC_ON_PUBLICATION.$EIC_ID = $ACCOUNT.$ID
        WHERE $EIC_ON_PUBLICATION.$PUBLICATION_ID = :$PUBLICATION_ID
    """)
    fun eicEmailsByPublicationId(@Param(PUBLICATION_ID) publicationId: Int): List<String>

    @Modifying
    @Query("INSERT INTO $EIC_ON_PUBLICATION ($PUBLICATION_ID, $EIC_ID) VALUES (:$PUBLICATION_ID, :$EIC_ID)")
    fun assign(@Param(PUBLICATION_ID) publicationId: Int, @Param(EIC_ID) eicId: Int)

    @Modifying
    @Query("DELETE FROM $EIC_ON_PUBLICATION WHERE $PUBLICATION_ID = :$PUBLICATION_ID AND $EIC_ID = :$EIC_ID")
    fun revoke(@Param(PUBLICATION_ID) publicationId: Int, @Param(EIC_ID) eicId: Int)
}