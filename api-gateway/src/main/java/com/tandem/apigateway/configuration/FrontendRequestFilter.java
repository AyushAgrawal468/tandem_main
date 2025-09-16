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
import com.tandemit.security.AESUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class FrontendRequestFilter implements WebFilter {

    @Value("${frontend.secret}")
    private String frontendSecret;

    @Value("${security.key}")
    private String secretKey;

    Logger logger = LoggerFactory.getLogger(FrontendRequestFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 1. Create token with secret + timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String valueToEncrypt = frontendSecret + ";" + timestamp;

        String encrypted = null;
        try {
            encrypted = AESUtil.encrypt(valueToEncrypt, secretKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Encrypted: " + encrypted);


        String header = exchange.getRequest().getHeaders().getFirst("X-Frontend-Secret");
        String decryptedSecret = null;
        Boolean isSecretValid = false;
        try {
            decryptedSecret = header != null ? (String) AESUtil.decrypt(header, secretKey, String.class) : null;
            isSecretValid = isValid(decryptedSecret);
        }catch (Exception ex){

            logger.error("Decryption error: ", ex);
        }

        if (!path.startsWith("/eureka") && (header == null || !isSecretValid)) {
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

    private boolean isValid(String decryptedValue) {
        try {
            String[] parts = decryptedValue.split(";");
            if (parts.length < 2) {
                return false;
            }
            String secretPart = parts[0]; // first part is secret
            String dateTimePart = parts[parts.length - 1]; // last part is timestamp
            LocalDateTime tokenTime = LocalDateTime.parse(dateTimePart, DateTimeFormatter.ISO_LOCAL_DATE_TIME); //YYYYMMDDTHH:MM:SS

            LocalDateTime now = LocalDateTime.now();
            Duration diff = Duration.between(tokenTime, now);

            return Math.abs(diff.toMinutes()) < 2 && frontendSecret.equals(secretPart) ; // strictly less than 2 mins
        } catch (Exception e) {
            return false;
        }
    }
}
