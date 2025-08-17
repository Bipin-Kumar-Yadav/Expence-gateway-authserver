package com.expence.gateway_authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity security) throws Exception {
            security
                .cors(cors-> cors.disable())
                .csrf(csrf-> csrf.disable())
                .authorizeExchange(req ->
                    req
                        .pathMatchers("/api/v1/users/*")
                        .authenticated()
                        .anyExchange().permitAll()
                );
            
            security
                .oauth2ResourceServer(authServer ->
                    authServer.jwt(Customizer.withDefaults())
                );

            return security.build();
    }
}
