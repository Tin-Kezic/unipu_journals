package hr.unipu.journals.data.repository

import hr.unipu.journals.data.entity.PublicationSection
import org.springframework.data.repository.CrudRepository

interface PublicationSectionRepository: CrudRepository<PublicationSection, Int>