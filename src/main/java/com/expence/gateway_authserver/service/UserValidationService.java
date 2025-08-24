package com.expence.gateway_authserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.expence.gateway_authserver.dto.UserResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserValidationService {
    @Autowired
    private WebClient userWebClient;

    public Mono<Boolean> validateUser(String userId){
        log.info("Validating user with ID: {}", userId);
        return userWebClient.get()
                .uri("/api/users/{userId}/validate",userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(WebClientResponseException.class, e->{
                    if(e.getStatusCode() == HttpStatus.NOT_FOUND){
                        return Mono.error(new RuntimeException("User not found:" + userId));
                    }
                    else if(e.getStatusCode() == HttpStatus.BAD_REQUEST){
                        return Mono.error(new RuntimeException("Invalid User Id"+userId));
                    }

                    return Mono.error(new RuntimeException("Unexpected error occurred while validating user: "+userId));
                });
    }

    public Mono<UserResponse> registerUser(UserResponse registerRequest){
        return userWebClient.post()
                .uri("/api/users/register")
                .bodyValue(registerRequest)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(WebClientResponseException.class, e->{
                    if(e.getStatusCode() == HttpStatus.BAD_REQUEST){
                        return Mono.error(new RuntimeException("Bad request while registering user: "+registerRequest.getUserId()));
                    }
                    else if(e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR){
                        return Mono.error(new RuntimeException("Server error occurred while registering user: "+registerRequest.getUserId()));
                    }
                    return Mono.error(new RuntimeException("Unexpected error occurred while registering user: "+registerRequest.getUserId()));
                });
    }

}
