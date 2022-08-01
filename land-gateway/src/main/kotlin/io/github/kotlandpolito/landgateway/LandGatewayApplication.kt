package io.github.kotlandpolito.landgateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication
@EnableEurekaClient
class LandGatewayApplication

fun main(args: Array<String>) {
	runApplication<LandGatewayApplication>(*args)
}
