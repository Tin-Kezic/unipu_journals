package hr.unipu.journals.feature.manuscript

import org.springframework.data.repository.Repository

private const val MANUSCRIPT = "manuscript"
private const val ID = "id"
private const val AUTHOR_ID = "author_id"
private const val CATEGORY_ID = "category_id"
private const val CURRENT_STATE = "current_state"
private const val SECTION_ID = "section_id"
private const val FILE_URL = "file_url"
private const val SUBMISSION_DATE = "submission_date"
private const val PUBLICATION_DATE = "publication_date"
private const val VIEWS = "views"
private const val DOWNLOADS = "downloads"
interface ManuscriptRepository: Repository<Manuscript, Int> {
    
}