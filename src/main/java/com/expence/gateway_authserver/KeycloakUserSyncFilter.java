package com.expence.gateway_authserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.expence.gateway_authserver.dto.UserResponse;
import com.expence.gateway_authserver.service.UserValidationService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class KeycloakUserSyncFilter implements WebFilter{
    @Autowired 
    private UserValidationService userValidationService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain){
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            log.debug("No valid Authorization header found. Proceeding without user sync.");
            return chain.filter(exchange);
        }
        UserResponse userResponse = getUserDetails(token);
        if (userResponse == null || userResponse.getUserId() == null) {
                log.warn("Unable to extract user details from token. Proceeding without user sync.");
                return chain.filter(exchange);
        }

        return userValidationService.validateUser(userResponse.getUserId())
                .flatMap(exists -> {
                    if(!exists){
                        log.info("User with ID: {} does not exist. Registering user.", userResponse.getUserId());
                        return Mono.empty();
                    } else {
                        log.info("User with ID: {} exists. Proceeding with the request.", userResponse.getUserId());
                        return Mono.empty();
                    }
                })
                .then(Mono.defer(()->{
                    org.springframework.http.server.reactive.ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", userResponse.getUserId())
                        .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                }));
    }

    private UserResponse getUserDetails(String token){
        try {
            String tokenWithoutBearer = token.replace("Bearer ","").trim();
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            UserResponse userResponse = new UserResponse();
            userResponse.setUserId(claims.getStringClaim("sub"));
            userResponse.setEmail(claims.getStringClaim("email"));
            userResponse.setFirstName(claims.getStringClaim("given_name"));
            userResponse.setLastName(claims.getStringClaim("family_name"));
            userResponse.setProfilePicUrl(claims.getStringClaim("profilePicUrl"));
            
            return userResponse;
        } catch (Exception e) {
           throw new RuntimeException("Error parsing token", e);
        }
    }
}
