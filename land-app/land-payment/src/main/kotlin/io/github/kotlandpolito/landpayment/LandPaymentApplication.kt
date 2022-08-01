package io.github.kotlandpolito.landpayment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class LandPaymentApplication

fun main(args: Array<String>) {
    runApplication<LandPaymentApplication>(*args)
}
