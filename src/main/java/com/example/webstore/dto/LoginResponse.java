package com.example.webstore.dto;

import io.quarkus.qute.TemplateData;

@TemplateData
public class LoginResponse {
    public String message;

    public LoginResponse(String message) {
        this.message = message;
    }
}
