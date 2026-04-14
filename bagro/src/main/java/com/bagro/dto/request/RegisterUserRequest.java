package com.bagro.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserRequest {

    private String username;
    private String password;
    private String role;
    private boolean active;
}