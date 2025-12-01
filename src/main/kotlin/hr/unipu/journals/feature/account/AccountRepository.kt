package hr.unipu.journals.feature.account

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface AccountRepository: Repository<Account, Int> {
    @Modifying
    @Query("UPDATE account SET is_admin = :is_admin WHERE email = :email")
    fun updateIsAdmin(@Param("email") email: String, @Param("is_admin") isAdmin: Boolean): Int

    @Modifying
    @Query("UPDATE account set password = :password WHERE email = 'root@unipu.hr'")
    fun updateRootPassword(@Param("password") password: String): Int

    @Query("SELECT EXISTS (SELECT 1 FROM account WHERE id = :id)")
    fun existsById(@Param("id") id: Int): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM account WHERE email = :email)")
    fun existsByEmail(@Param("email") email: String): Boolean

    @Query("SELECT * FROM account WHERE id = :id")
    fun byId(@Param("id") id: Int): Account?

    @Query("SELECT * FROM account WHERE email = :email")
    fun byEmail(@Param("email") email: String): Account?

    @Query("SELECT email FROM account WHERE is_admin = TRUE")
    fun allAdminEmails(): List<String>

    // todo. check if postgresql migration broke dto param
    @Modifying
    @Query("""
        INSERT INTO account
        (full_name, title, email, password, affiliation, job_type, country, city, address, zip_code)
        VALUES
        (:#{#dto.fullName}, :#{#dto.title}, :#{#dto.email}, :#{#dto.password}, :#{#dto.affiliation}, :#{#dto.jobType}, :#{#dto.country}, :#{#dto.city}, :#{#dto.address}, :#{#dto.zipCode})
    """)
    fun insert(@Param("dto") accountDTO: AccountDTO): Int

    // todo. check if postgresql migration broke dto param
    @Modifying
    @Query("""
        UPDATE account SET
        full_name = :#{#dto.fullName},
        title = :#{#dto.title},
        email = :#{#dto.email},
        password = :#{#dto.password},
        affiliation = :#{#dto.affiliation},
        job_type = :#{#dto.jobType},
        country = :#{#dto.country},
        city = :#{#dto.city},
        address = :#{#dto.address},
        zip_code = :#{#dto.zipCode}
        WHERE id = :id
    """)
    fun update(
        @Param("id") id: Int,
        @Param("dto") accountDTO: AccountDTO
    ): Int

    @Modifying
    @Query("DELETE FROM account WHERE id = :id")
    fun delete(@Param("id") id: Int): Int
}