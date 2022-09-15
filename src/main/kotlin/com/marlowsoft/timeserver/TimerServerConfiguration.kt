package com.marlowsoft.timeserver

import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class TimerServerConfiguration {
    @Bean
    fun getDataSource(): DataSource {
        val dataSourceBuilder = DataSourceBuilder.create()

        dataSourceBuilder
            .driverClassName("org.postgresql.Driver")
            .url("jdbc:postgresql://localhost:55000/postgres")
            .username("postgres")
            .password("postgrespw")

        return dataSourceBuilder.build()
    }
}
