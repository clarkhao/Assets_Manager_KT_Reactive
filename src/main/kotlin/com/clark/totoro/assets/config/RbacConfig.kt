package com.clark.totoro.assets.config

import org.casbin.adapter.JDBCAdapter
import org.casbin.jcasbin.main.Enforcer
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RbacConfig {
    @Value("\${spring.datasource.driver-class-name}")
    val driver: String = ""
    @Value("\${spring.datasource.url}")
    val url: String = ""
    @Value("\${spring.datasource.username}")
    val username: String = ""
    @Value("\${spring.datasource.password}")
    val password: String = ""

    @Bean
    fun cas(): Enforcer {
        //var adapter = JDBCAdapter(driver, url, username, password)
        val dataSource = PGSimpleDataSource()
        dataSource.setURL(url)
        dataSource.user = username
        dataSource.password = password
        val adapter = JDBCAdapter(dataSource)
        return Enforcer("conf/model.conf", adapter)
    }
}