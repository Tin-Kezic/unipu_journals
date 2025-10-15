package hr.unipu.journals.feature.account

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface AccountRepository: Repository<Account, Int> {

    @Query("SELECT is_admin FROM account WHERE email = :email")
    fun isAdmin(@Param("email") email: String): Boolean

    @Modifying
    @Query("UPDATE account SET is_admin = :is_admin WHERE email = :email")
    fun updateIsAdmin(@Param("email") email: String, @Param("is_admin") isAdmin: Boolean)

    @Modifying
    @Query("UPDATE account set password = :password WHERE email = :email")
    fun updatePassword(@Param("email") email: String, @Param("password") password: String)

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

    @Modifying
    @Query("""
        INSERT INTO account
        (full_name, title, email, password, affiliation, job_type, country, city, address, zip_code)
        VALUES
        (:full_name, :title, :email, :password, :affiliation, :job_type, :country, :city, :address, :zip_code)
    """)
    fun insert(
        @Param("full_name") fullName: String,
        @Param("title") title: String,
        @Param("email") email: String,
        @Param("password") password: String,
        @Param("affiliation") affiliation: String,
        @Param("job_type") jobType: String,
        @Param("country") country: String,
        @Param("city") city: String,
        @Param("address") address: String,
        @Param("zip_code") zipCode: String,
    )
    @Modifying
    @Query("""
        UPDATE account SET
        full_name = :full_name,
        title = :title,
        email = :email,
        password = :password,
        affiliation = :affiliation,
        job_type = :job_type,
        country = :country,
        city = :city,
        address = :address,
        zip_code = :zip_code
        WHERE id = :id
    """)
    fun update(
        @Param("id") id: Int,
        @Param("full_name") fullName: String,
        @Param("title") title: String,
        @Param("email") email: String,
        @Param("password") password: String,
        @Param("affiliation") affiliation: String,
        @Param("job_type") jobType: String,
        @Param("country") country: String,
        @Param("city") city: String,
        @Param("address") address: String,
        @Param("zip_code") zipCode: String
    )
    @Modifying
    @Query("DELETE FROM account WHERE id = :id")
    fun delete(@Param("id") id: Int)
}