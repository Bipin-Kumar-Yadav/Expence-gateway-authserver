package com.expence.gateway_authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
            security
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                    req
                        .requestMatchers("/api/v1/users/*")
                        .authenticated()
                        .anyRequest().permitAll()
                );
            
            security
                .oauth2ResourceServer(authServer ->
                    authServer.jwt(Customizer.withDefaults())
                );

            return security.build();
    }
}
