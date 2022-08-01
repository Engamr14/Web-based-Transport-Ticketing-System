package io.github.kotlandpolito.lab3

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
// Enables scheduling only on methods marked as @Scheduled useful in testing to not apply scheduling overall
@ConditionalOnProperty(name = ["scheduler.enabled"], matchIfMissing = true)
class SchedulerConfig
