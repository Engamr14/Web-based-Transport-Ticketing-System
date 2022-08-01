package io.github.kotlandpolito.lab3.user

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CrudRepository<LANDUser, Long> {
    @Query(value = "select distinct * from landuser u where u.username = :username limit 1", nativeQuery = true)
    fun findByUsername(@Param("username") username: String): LANDUser?

    @Query(value = "select distinct * from landuser u where u.email = :email limit 1", nativeQuery = true)
    fun findByEmail(@Param("email") email: String): LANDUser?

    @Query(value = "select distinct * from landuser u where u.id = :id", nativeQuery = true)
    fun findbyId(@Param("id") id: Long?): LANDUser?

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