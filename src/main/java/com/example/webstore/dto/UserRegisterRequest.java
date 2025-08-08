package com.example.webstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegisterRequest {

    @NotBlank(message = "Username is required")
    public String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    public String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    public String password;

    public String firstName;
    public String lastName;
    public String phone;
    public String address;
    public String city;
}
