package org.gft.gbt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
public class SwaggerSecurityConfig {

    @Bean
    public WebSecurityCustomizer swaggerWebSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**"
        );
    }
}
