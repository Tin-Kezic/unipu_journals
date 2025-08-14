package hr.unipu.journals.feature.account

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param


private const val ACCOUNT = "account"
private const val ID = "id"
private const val NAME = "name"
private const val SURNAME = "surname"
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

    @Query("SELECT * FROM $ACCOUNT WHERE $EMAIL = :$EMAIL")
    fun byEmail(@Param(EMAIL) email: String): Account

    @Query("SELECT EXISTS (SELECT 1 FROM $ACCOUNT WHERE $EMAIL = :$EMAIL")
    fun emailExists(@Param(EMAIL) email: String): Boolean
}