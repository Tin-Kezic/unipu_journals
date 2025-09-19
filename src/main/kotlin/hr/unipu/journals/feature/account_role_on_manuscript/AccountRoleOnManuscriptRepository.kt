package hr.unipu.journals.feature.account_role_on_manuscript

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

private const val ID = "id"
private const val ACCOUNT_ROLE_ON_MANUSCRIPT = "account_role_on_manuscript"
private const val MANUSCRIPT_ID = "manuscript_id"
private const val ACCOUNT_ID = "account_id"
private const val ACCOUNT_ROLE = "account_role"

// manuscript_role
private const val EIC = "'EIC'"
private const val EDITOR = "'EDITOR'"
private const val REVIEWER = "'REVIEWER'"
private const val CORRESPONDING_AUTHOR = "'CORRESPONDING_AUTHOR'"
private const val AUTHOR = "'AUTHOR'"

// manuscript
private const val MANUSCRIPT = "manuscript"
private const val TITLE = "title"
private const val DESCRIPTION = "description"
private const val AUTHOR_ID = "author_id"
private const val CATEGORY_ID = "category_id"
private const val CURRENT_STATE = "current_state"
private const val SECTION_ID = "section_id"
private const val FILE_URL = "file_url"
private const val SUBMISSION_DATE = "submission_date"
private const val PUBLICATION_DATE = "publication_date"
private const val VIEWS = "views"
private const val DOWNLOADS = "downloads"

// account
private const val ACCOUNT = "account"
private const val FULL_NAME = "full_name"
private const val EMAIL = "email"
private const val PASSWORD = "password"
private const val AFFILIATION = "affiliation"
private const val JOB_TYPE = "job_type"
private const val COUNTRY = "country"
private const val CITY = "city"
private const val ADDRESS = "address"
private const val ZIP_CODE = "zip_code"
private const val IS_ADMIN = "is_admin"

interface AccountRoleOnManuscriptRepository : Repository<AccountRoleOnManuscript, Int> {
    @Query("""
        SELECT DISTINCT $ACCOUNT.$FULL_NAME FROM $ACCOUNT_ROLE_ON_MANUSCRIPT
        JOIN $ACCOUNT ON $ACCOUNT.$ID = $ACCOUNT_ROLE_ON_MANUSCRIPT.$ACCOUNT_ID
        JOIN $MANUSCRIPT ON $MANUSCRIPT.$ID = $ACCOUNT_ROLE_ON_MANUSCRIPT.$MANUSCRIPT_ID
        WHERE $MANUSCRIPT.$ID = :$MANUSCRIPT_ID
        AND ($ACCOUNT_ROLE_ON_MANUSCRIPT.$ACCOUNT_ROLE = $CORRESPONDING_AUTHOR OR $ACCOUNT_ROLE_ON_MANUSCRIPT.$ACCOUNT_ROLE = $AUTHOR)
    """)
    fun authors(@Param(MANUSCRIPT_ID) manuscriptId: Int): List<String>
    @Query("""
        SELECT DISTINCT $ACCOUNT.$FULL_NAME FROM $ACCOUNT_ROLE_ON_MANUSCRIPT
        JOIN $ACCOUNT ON $ACCOUNT.$ID = $ACCOUNT_ROLE_ON_MANUSCRIPT.$ACCOUNT_ID
        JOIN $MANUSCRIPT ON $MANUSCRIPT.$ID = $ACCOUNT_ROLE_ON_MANUSCRIPT.$MANUSCRIPT_ID
        WHERE $MANUSCRIPT.$ID = :$MANUSCRIPT_ID
        AND $ACCOUNT_ROLE_ON_MANUSCRIPT.$ACCOUNT_ROLE = $CORRESPONDING_AUTHOR
    """)
    fun correspondingAuthor(@Param(MANUSCRIPT_ID) manuscriptId: Int): String
    @Query("""
        SELECT EXISTS (SELECT 1 FROM $ACCOUNT_ROLE_ON_MANUSCRIPT
        WHERE $MANUSCRIPT_ID = :$MANUSCRIPT_ID AND $ACCOUNT_ID = :$ACCOUNT_ID
        AND $ACCOUNT_ROLE = 'EDITOR'
    """)
    fun isEditorOnManuscript(@Param(ACCOUNT_ID) editorId: Int, @Param(MANUSCRIPT_ID) manuscriptId: Int): Boolean
    @Query("""
        SELECT EXISTS (SELECT 1 FROM $ACCOUNT_ROLE_ON_MANUSCRIPT
        WHERE $MANUSCRIPT_ID = :$MANUSCRIPT_ID AND $ACCOUNT_ID = :$ACCOUNT_ID
        AND $ACCOUNT_ROLE = 'REVIEWER'
    """)
    fun isReviewerOnManuscript(@Param(ACCOUNT_ID) reviewerId: Int, @Param(MANUSCRIPT_ID) manuscriptId: Int): Boolean
    @Query("""
        SELECT EXISTS (SELECT 1 FROM $ACCOUNT_ROLE_ON_MANUSCRIPT
        WHERE $MANUSCRIPT_ID = :$MANUSCRIPT_ID AND $ACCOUNT_ID = :$ACCOUNT_ID
        AND $ACCOUNT_ROLE = 'CORRESPONDING_AUTHOR'
    """)
    fun isCorrespondingAuthorOnManuscript(@Param(ACCOUNT_ID) correspondingAuthorId: Int, @Param(MANUSCRIPT_ID) manuscriptId: Int): Boolean
    @Query("""
        SELECT EXISTS (SELECT 1 FROM $ACCOUNT_ROLE_ON_MANUSCRIPT
        WHERE $MANUSCRIPT_ID = :$MANUSCRIPT_ID AND $ACCOUNT_ID = :$ACCOUNT_ID
        AND $ACCOUNT_ROLE = 'AUTHOR'
    """)
    fun isAuthorOnManuscript(@Param(ACCOUNT_ID) authorId: Int, @Param(MANUSCRIPT_ID) manuscriptId: Int): Boolean
}

