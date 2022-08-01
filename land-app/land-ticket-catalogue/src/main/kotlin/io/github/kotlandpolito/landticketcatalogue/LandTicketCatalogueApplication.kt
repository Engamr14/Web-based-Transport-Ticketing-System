package io.github.kotlandpolito.landticketcatalogue

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.server.reactive.HttpHandler
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.server.adapter.WebHttpHandlerBuilder
import reactor.netty.DisposableServer
import reactor.netty.http.server.HttpServer

@SpringBootApplication
@EnableDiscoveryClient
class LandTicketCatalogueApplication {
}

fun main(args: Array<String>) {
    runApplication<LandTicketCatalogueApplication>(*args)
}