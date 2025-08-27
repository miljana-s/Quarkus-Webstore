package com.example.webstore.dto;

import io.quarkus.qute.TemplateData;

@TemplateData
public class UserDTO {
    public Long id;
    public String firstName;
    public String lastName;
    public String email;
    public String phone;
    public String address;
    public String city;
}
