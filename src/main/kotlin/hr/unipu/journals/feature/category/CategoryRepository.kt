package hr.unipu.journals.feature.category

import org.springframework.data.repository.Repository

private const val ID = "id"
private const val NAME = "name"
interface CategoryRepository: Repository<Category, Int>
