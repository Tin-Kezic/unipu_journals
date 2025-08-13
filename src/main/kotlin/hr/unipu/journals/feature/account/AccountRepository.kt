package hr.unipu.journals.feature.account

import org.springframework.data.repository.Repository

interface AccountRepository: Repository<Account, Int> {

    @Query("SELECT * FROM $ACCOUNT WHERE $EMAIL = :$EMAIL")
    fun byEmail(@Param(EMAIL) email: String)

    @Query("SELECT EXISTS (SELECT 1 FROM $ACCOUNT WHERE $EMAIL = :$EMAIL")
    fun emailExists(@Param(EMAIL) email: String): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM $ACCOUNT WHERE $IS_ADMIN = TRUE AND $ID = :$ID")
    fun isAdmin(@Param(ID) id: Int): Boolean
}