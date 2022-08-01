package io.github.kotlandpolito.lab4traveler

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TicketRepository : CrudRepository<TicketPurchased, Long> {

    @Query("select * from ticket_purchased tp where tp.user_id = :userId", nativeQuery = true)
    fun findByUserId(@Param("userId") userId: String): Iterable<TicketPurchased>

    fun findByJws(jws: String): TicketPurchased?

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