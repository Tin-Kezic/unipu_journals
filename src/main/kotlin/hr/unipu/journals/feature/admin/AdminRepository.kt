package hr.unipu.journals.feature.admin

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val ADMIN = "admin"
private const val ID = "id"
private const val EMAIL = "email"
interface AdminRepository: Repository<Admin, Int> {
    @Query("SELECT EXISTS (SELECT 1 FROM $ADMIN WHERE $EMAIL = :$EMAIL)")
    fun isAdmin(@Param(EMAIL) email: String): Boolean
}
