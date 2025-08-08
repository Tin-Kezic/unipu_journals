package hr.unipu.journals.data.repository

import hr.unipu.journals.data.entity.Category
import org.springframework.data.repository.CrudRepository

interface CategoryRepository: CrudRepository<Category, Int>
