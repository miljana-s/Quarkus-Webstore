package com.example.webstore.dto;

import io.quarkus.qute.TemplateData;

@TemplateData
public class CategoryDTO {
    public Long id;
    public String name;

    public CategoryDTO() {}

    public CategoryDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
