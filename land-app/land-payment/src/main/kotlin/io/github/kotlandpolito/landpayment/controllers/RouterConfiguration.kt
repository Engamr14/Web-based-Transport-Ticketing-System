package io.github.kotlandpolito.landpayment.controllers

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class RouterConfiguration {

    @Bean
    fun mainRouter(transactionHandler: TransactionHandler) = coRouter {
        accept(APPLICATION_JSON).nest {
            GET("/transactions", transactionHandler::getAllTransactionsOfSingleUser)
            GET("/admin/transactions", transactionHandler::getAllTransactionsOfAllUsers)
        }
    }

}