package hr.unipu.journals.data.repository

import hr.unipu.journals.data.entity.Manuscript
import org.springframework.data.repository.CrudRepository

interface ManuscriptRepository: CrudRepository<Manuscript, Int>