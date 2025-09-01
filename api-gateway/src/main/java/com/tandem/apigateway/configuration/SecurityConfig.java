package com.tandem.apigateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final FrontendRequestFilter frontendWebFilter;

    public SecurityConfig(FrontendRequestFilter frontendWebFilter) {
        this.frontendWebFilter = frontendWebFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/**").permitAll()
                        .anyExchange().authenticated()
                )
                // 👇 Register your custom filter before AUTHENTICATION stage
                .addFilterBefore(frontendWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

}
// Note: FrontendRequestFilter should be a @Component and will be auto-applied as a GlobalFilter.
