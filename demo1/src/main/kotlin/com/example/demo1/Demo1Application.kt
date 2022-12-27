package com.example.demo1

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
import org.springframework.stereotype.Component

@SpringBootApplication
class Demo1Application

fun main(args: Array<String>) {
    runApplication<Demo1Application>(*args)
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
        logger.info("[DEMO1] MESSAGE RECEVED -> [$message]")
    }
}
