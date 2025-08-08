package com.example.webstore.dto;

import com.example.webstore.model.User;

public class UserResponse {

    public Long id;
    public String username;
    public String email;
    public String firstName;
    public String lastName;
    public String phone;
    public String address;
    public String city;

    public UserResponse(User user) {
        this.id = user.id;
        this.username = user.username;
        this.email = user.email;
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.phone = user.phone;
        this.address = user.address;
        this.city = user.city;
    }
}
