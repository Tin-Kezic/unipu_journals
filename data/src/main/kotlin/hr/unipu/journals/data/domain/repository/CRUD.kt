package hr.unipu.journals.data.domain.repository

import hr.unipu.journals.data.domain.entity.Account
import java.util.Optional

/*
Methods from CrudRepository<T, ID> made into a custom interface.
Because inheriting it directly would require the jdbc dependency outside the :data module
 */
interface CRUD {
    fun <S : Account?> save(entity: S & Any): S & Any
    fun <S : Account?> saveAll(entities: Iterable<S?>): Iterable<S?>
    fun findById(id: Long): Optional<Account?>
    fun existsById(id: Long): Boolean
    fun findAll(): Iterable<Account?>
    fun findAllById(ids: Iterable<Long?>): Iterable<Account?>
    fun count(): Long
    fun deleteById(id: Long)
    fun delete(entity: Account)
    fun deleteAllById(ids: Iterable<Long?>)
    fun deleteAll(entities: Iterable<Account?>)
    fun deleteAll()
}