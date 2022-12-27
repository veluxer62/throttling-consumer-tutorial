package com.example.demo

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.BucketProxy
import io.github.bucket4j.distributed.jdbc.SQLProxyConfiguration
import io.github.bucket4j.postgresql.PostgreSQLadvisoryLockBasedProxyManager
import java.time.Duration
import javax.sql.DataSource
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

@RestController
class Controller(
    private val jmsTemplate: JmsTemplate,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/test")
    fun test() {
        repeat(20) {
            logger.info("[DEMO] MESSAGE SENT!!!")
            jmsTemplate.convertAndSend("SAMPLE", "TEST MESSAGE GOGO")
        }
    }
}

@Configuration
class BucketConfig{
    @Bean
    fun proxyManager(dataSource: DataSource): PostgreSQLadvisoryLockBasedProxyManager {
        return PostgreSQLadvisoryLockBasedProxyManager(SQLProxyConfiguration(dataSource))
    }

    @Bean
    fun bucket(proxyManager: PostgreSQLadvisoryLockBasedProxyManager): BucketProxy {
        val key = 3L
        val bucketConfiguration = BucketConfiguration.builder()
            .addLimit(Bandwidth.simple(5, Duration.ofSeconds(1)))
            .build()
        return proxyManager.builder().build(key, bucketConfiguration)
    }
}

@Component
class Receiver(
    private val bucket: BucketProxy
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @JmsListener(destination = "SAMPLE")
    fun handle(message: String) {
        bucket.asBlocking().consume(1)
        logger.info("[DEMO] MESSAGE RECEVED -> [$message]")
    }
}
