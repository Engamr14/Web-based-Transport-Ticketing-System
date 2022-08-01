package io.github.kotlandpolito.lab4traveler

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : CrudRepository<UserDetails, String> {

    fun findByName(name: String): Optional<UserDetails>

    /* PREDEFINED METHODS
    * * count(): long
    * delete(entity: T)
    * deleteAll()
    * exists(id: Id): boolean
    * findAll(): Iterable<T>
    * findbyId(id: Id): Optional<T>
    * save(entity: T): T
    * */
}