package io.github.kotlandpolito.lab3.interceptors

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Bucket4j
import io.github.bucket4j.Refill
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import java.time.Duration
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


// Source: https://www.baeldung.com/spring-bucket4j

@Profile("!rate-limit-test")
@Component
class RateLimiterInterceptor : HandlerInterceptorAdapter() {

    // 10 requests per second
    var tokenBucket: Bucket = Bucket4j.builder()
        .addLimit(Bandwidth.classic(10, Refill.greedy(10, Duration.ofSeconds(1))))
        .build()

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        val probe = tokenBucket.tryConsumeAndReturnRemaining(1)
        return if (probe.isConsumed) {
            response.addHeader("X-Rate-Limit-Remaining", probe.remainingTokens.toString())
            true
        } else {
            val waitForRefill = probe.nanosToWaitForRefill / 1000000000
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", waitForRefill.toString())
            response.sendError(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "You have exceeded the API Rate Limit. Please try again in $waitForRefill seconds."
            )
            false
        }
    }
}

@Profile("rate-limit-test")
@Component
class RateLimiterInterceptorMock : RateLimiterInterceptor() {

    // Mock works in the same way, except a tighter rate is set (1 request per minute), for easier testing
    init {
        super.tokenBucket =
            Bucket4j.builder().addLimit(Bandwidth.classic(1, Refill.greedy(1, Duration.ofMinutes(1)))).build()
    }

}