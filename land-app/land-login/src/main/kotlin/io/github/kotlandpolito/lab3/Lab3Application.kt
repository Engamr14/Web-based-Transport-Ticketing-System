package io.github.kotlandpolito.lab3

import io.github.kotlandpolito.lab3.interceptors.RateLimiterInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

//import org.springframework.scheduling.annotation.EnableScheduling

//@EnableScheduling
@SpringBootApplication
@EnableDiscoveryClient
class Lab3Application : WebMvcConfigurer {

    @Autowired
    lateinit var interceptor: RateLimiterInterceptor

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(interceptor)
            .addPathPatterns("/auth/**")
    }
}

fun main(args: Array<String>) {
    runApplication<Lab3Application>(*args)
}
