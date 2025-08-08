package com.example.webstore.dto;

public class CategoryDTO {
    public Long id;
    public String name;

    public CategoryDTO() {}

    public CategoryDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
