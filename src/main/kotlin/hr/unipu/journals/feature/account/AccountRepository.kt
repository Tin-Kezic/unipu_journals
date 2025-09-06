package hr.unipu.journals.feature.account

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val ACCOUNT = "account"
private const val ID = "id"
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

interface AccountRepository: Repository<Account, Int> {

    @Query("SELECT EXISTS (SELECT 1 FROM $ACCOUNT WHERE $EMAIL = :$EMAIL AND $IS_ADMIN = TRUE)")
    fun isAdmin(@Param(EMAIL) email: String): Boolean

    @Modifying
    @Query("UPDATE $ACCOUNT SET $IS_ADMIN = :$IS_ADMIN WHERE $EMAIL = :$EMAIL")
    fun updateIsAdmin(@Param(EMAIL) email: String, @Param(IS_ADMIN) isAdmin: Boolean)

    @Modifying
    @Query("UPDATE $ACCOUNT SET $PASSWORD = :$PASSWORD WHERE $EMAIL = 'root@unipu.hr'")
    fun updateRootPassword(@Param(PASSWORD) password: String)

    @Query("SELECT EXISTS (SELECT 1 FROM $ACCOUNT WHERE $ID = :$ID)")
    fun exists(@Param(ID) id: Int): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM $ACCOUNT WHERE $EMAIL = :$EMAIL)")
    fun emailExists(@Param(EMAIL) email: String): Boolean

    @Query("SELECT * FROM $ACCOUNT WHERE $EMAIL = :$EMAIL")
    fun byEmail(@Param(EMAIL) email: String): Account?

    @Query("SELECT $EMAIL FROM $ACCOUNT WHERE $IS_ADMIN = TRUE")
    fun allAdminEmails(): List<String>

    @Modifying
    @Query("""
        INSERT INTO $ACCOUNT
        ($FULL_NAME, $TITLE, $EMAIL, $PASSWORD, $AFFILIATION, $JOB_TYPE, $COUNTRY, $CITY, $ADDRESS, $ZIP_CODE)
        VALUES
        (:$FULL_NAME, :$TITLE, :$EMAIL, :$PASSWORD, :$AFFILIATION, :$JOB_TYPE, :$COUNTRY, :$CITY, :$ADDRESS, :$ZIP_CODE)
    """)
    fun insert(
        @Param(FULL_NAME) fullName: String,
        @Param(TITLE) title: String,
        @Param(EMAIL) email: String,
        @Param(PASSWORD) password: String,
        @Param(AFFILIATION) affiliation: String,
        @Param(JOB_TYPE) jobType: String,
        @Param(COUNTRY) country: String,
        @Param(CITY) city: String,
        @Param(ADDRESS) address: String,
        @Param(ZIP_CODE) zipCode: String,
    )
    @Modifying
    @Query("DELETE FROM $ACCOUNT WHERE $ID = :$ID")
    fun delete(@Param(ID) id: Int)
}