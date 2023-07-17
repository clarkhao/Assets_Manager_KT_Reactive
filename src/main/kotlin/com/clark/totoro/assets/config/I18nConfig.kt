package com.clark.totoro.assets.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver
import java.util.Locale

@Configuration
class I18nConfig {
    @Bean
    fun localeResolver(): AcceptHeaderLocaleContextResolver {
        val localeResolver = AcceptHeaderLocaleContextResolver()
        localeResolver.defaultLocale = Locale.ENGLISH
        return localeResolver
    }

}