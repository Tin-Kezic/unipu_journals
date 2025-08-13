package hr.unipu.journals.feature.eic_on_publication

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val EIC_ON_PUBLICATION = "eic_on_publication"
private const val ID = "id"
private const val PUBLICATION_ID = "publication_id"
private const val EIC_ID = "eic_id"
interface EicOnPublicationRepository: Repository<EicOnPublication, Int> {
    @Query("SELECT EXISTS (SELECT 1 FROM $EIC_ON_PUBLICATION WHERE $PUBLICATION_ID = :$PUBLICATION_ID AND $EIC_ID = :$EIC_ID")
    fun isEicOnPublication(@Param(PUBLICATION_ID) publicationId: Int, @Param(EIC_ID) eicId: Int): Boolean
}