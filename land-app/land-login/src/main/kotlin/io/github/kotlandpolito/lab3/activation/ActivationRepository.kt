package io.github.kotlandpolito.lab3.activation

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ActivationRepository : CrudRepository<LANDActivation, UUID> {

    /* PREDEFINED METHODS
    * count(): long
    * delete(entity: T)
    * deleteAll()
    * exists(id: Id): boolean
    * findAll(): Iterable<T>
    * findbyId(id: Id): Optional<T>
    * save(entity: T): T
    * */

}