package com.tandem.apigateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class FrontendRequestFilter implements WebFilter {

    @Value("${frontend.secret}")
    private String frontendSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Skip filter for Eureka endpoints
//        if (path.startsWith("/eureka")) {
//            return chain.filter(exchange);
//        }

        String header = exchange.getRequest().getHeaders().getFirst("X-Frontend-Secret");

        if (!path.startsWith("/eureka") && (header == null || !header.equals(frontendSecret))) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            String body = "{\"error\":\"Forbidden: Invalid or missing X-Frontend-Secret\"}";
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse()
                            .bufferFactory()
                            .wrap(body.getBytes())));
        }

        // ✅ Mark request as authenticated with a dummy principal
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "frontend-user",   // principal
                null,              // credentials
                List.of()          // authorities (roles) -> you can assign roles here
        );

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
    }
}
