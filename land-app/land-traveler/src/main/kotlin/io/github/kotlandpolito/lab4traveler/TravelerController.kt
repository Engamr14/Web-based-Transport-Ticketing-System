package io.github.kotlandpolito.lab4traveler

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.util.ConditionalOnBootstrapDisabled
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.io.File
import java.io.OutputStream
import java.util.*
import javax.servlet.http.HttpServletResponse
import kotlin.collections.ArrayList


@RestController
class TravelerController(val travelerService: TravelerService, val userRepo: UserRepository, val ticketRepo: TicketRepository) {

    @Value("\${server.jwt-key}")
    lateinit var jwtKey: String

    @GetMapping("/my/profile")
    fun getUserProfile(): UserDetailsDTO? {
        val currentUserOpt = userRepo.findByName(SecurityContextHolder.getContext().authentication.name)
        if (!currentUserOpt.isPresent){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found")
        }
        val currentUser = currentUserOpt.get()
        return currentUser.toUserDetailsDTO()
    }

    @PostMapping("/my/profile")
    fun insertUserDetails(@RequestBody profile: UserDetails, response: HttpServletResponse){
        if (!travelerService.insertProfile(profile))
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Profile not inserted")
        else
            response.status= HttpStatus.OK.value()
    }

    @PutMapping("/my/profile")
    fun updateUserProfile(@RequestBody updatedUser: UserDetailsUpdateRequestDTO) {
        val currentUserOpt = userRepo.findByName(SecurityContextHolder.getContext().authentication.name)
        if (!currentUserOpt.isPresent){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found")
        }
        val currentUser = currentUserOpt.get()

        currentUser.address = updatedUser.address
        currentUser.date_of_birth = updatedUser.date_of_birth
        currentUser.telephone_number = updatedUser.telephone_number
        userRepo.save(currentUser)
    }

    @GetMapping("/my/tickets")
    fun getUserTickets(): List<TicketPurchasedDTO>? {
        val currentUserOpt = userRepo.findByName(SecurityContextHolder.getContext().authentication.name)
        if (!currentUserOpt.isPresent){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found")
        }
        val currentUser = currentUserOpt.get()
        return currentUser.tickets.map { it.toTicketPurchasedDTO() }
    }

    /*
    @PostMapping("/tickets")
    fun buyUserTickets(@RequestBody ticketPurchase: TicketPurchaseRequestDTO) : List<TicketPurchasedDTO> {
        val currentUserOpt = userRepo.findByName(SecurityContextHolder.getContext().authentication.name)
        if (!currentUserOpt.isPresent){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found")
        }
        val currentUser = currentUserOpt.get()

        if (ticketPurchase.cmd != "buy_tickets"){
            throw ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Given cmd hasn't been implemented yet")
        }

        val newTickets = ArrayList<TicketPurchased>()
        for (i in 1..ticketPurchase.quantity){
            // create a ticket
            newTickets.add(TicketPurchased(ticketPurchase.zones, currentUser))
        }
        ticketRepo.saveAll(newTickets)

        // map newTickets to list of TicketPurchasedDTO
        val ticketDTOS:List<TicketPurchasedDTO> = newTickets.map { it.toTicketPurchasedDTO() }

        return ticketDTOS
    }
    */

    @GetMapping("/admin/travelers")
    fun getTravelers(): List<String>? {
        return userRepo.findAll().map { it.name }
    }

    @GetMapping("/admin/traveler/{userID}/profile")
    fun getTravelerProfile(@PathVariable("userID") userId: String): UserDetailsDTO? {
        val travelerOpt = userRepo.findById(userId)
        if (!travelerOpt.isPresent){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Id not found")
        }
        val traveler = travelerOpt.get()
        return traveler.toUserDetailsDTO()
    }

    @GetMapping("/admin/traveler/{userID}/tickets")
    fun getTravelerTickets(@PathVariable("userID") userId: String): List<TicketPurchasedDTO>? {
        val travelerOpt = userRepo.findById(userId)
        if (!travelerOpt.isPresent){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Id not found")
        }
        val traveler = travelerOpt.get()
        return traveler.tickets.map { it.toTicketPurchasedDTO() }
    }

    @PostMapping("/admin/traveler/{userID}/tickets")
    fun addTravelerTickets(
        @PathVariable("userID") userId: String,
        @RequestBody ticketAddition: TicketPurchaseAdditionDTO
    ): List<TicketPurchasedDTO>? {
        val travelerOpt = userRepo.findById(userId)
        if (!travelerOpt.isPresent) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Id not found")
        }
        val traveler = travelerOpt.get()

        val newTickets = ArrayList<TicketPurchased>()
        for (i in 1..ticketAddition.quantity) {
            // create a ticket
            var newTicket = TicketPurchased(ticketAddition.zid, traveler)
            newTicket.exp = ticketAddition.validfrom + ticketAddition.duration

            newTicket = ticketRepo.save(newTicket)

            val jwt = Jwts.builder().setHeaderParam("alg", "HS256")
                .setSubject(newTicket.id.toString())
                .setIssuedAt(Date(newTicket.iat))
                .setExpiration(Date(newTicket.exp))
                .claim("zid", ticketAddition.zid)
            newTicket.jws = jwt.signWith(Keys.hmacShaKeyFor(jwtKey.toByteArray())).compact()

            newTicket = ticketRepo.save(newTicket)
            newTickets.add(newTicket)
        }

        // map newTickets to list of TicketPurchasedDTO
        return newTickets.map { it.toTicketPurchasedDTO() }
    }

    @GetMapping("/tickets/download/{ticketId}")
    fun downloadTicket(@PathVariable("ticketId") ticketId: Long, response: HttpServletResponse){
        val currentUserOpt = userRepo.findByName(SecurityContextHolder.getContext().authentication.name)
        if (!currentUserOpt.isPresent){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found")
        }
        val currentUser = currentUserOpt.get()

        val currentUserTickets = currentUser.tickets
        currentUserTickets.find { it.id == ticketId } ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket for current user not found")

        response.contentType = "image/png"
        val qrCode: ByteArray? = travelerService.getQRCode(ticketId)
        val outputStream: OutputStream = response.outputStream
        outputStream.write(qrCode)
    }

    @GetMapping("/admin/transits")
    fun getTransits(): List<TransitDTO>?{
        return travelerService.getTransits()?.map { it.toTransitDTO() }
    }

    @GetMapping("/admin/traveler/{userId}/transits")
    fun getTransitsOfUser(@PathVariable("userId") userId: String): List<TransitDTO>?{
        return travelerService.getTransitsById(userId)?.map { it.toTransitDTO() }
    }


    @GetMapping("/ticket/validate/{ticketJWS}")
    fun validateTicket(@PathVariable ticketJWS: String): Boolean {
        if(travelerService.validateTicket(ticketJWS)){
            return true
        } else {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket not valid")
        }
    }

}