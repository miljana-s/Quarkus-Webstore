package com.example.webstore.dto;

import jakarta.validation.constraints.Size;

public class UserUpdateRequest {

    @Size(min = 3, message = "Username must be at least 3 characters")
    public String username;

    public String firstName;
    public String lastName;
    public String address;
    public String city;

    @Size(min = 6, message = "Phone number must be at least 6 characters")
    public String phone;
}
