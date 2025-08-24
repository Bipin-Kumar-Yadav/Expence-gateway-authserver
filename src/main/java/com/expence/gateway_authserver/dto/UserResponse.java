package com.expence.gateway_authserver.dto;

import lombok.Data;

@Data
public class UserResponse {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicUrl;
}
