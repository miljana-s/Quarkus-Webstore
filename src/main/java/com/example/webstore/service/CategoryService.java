package com.example.webstore.service;

import com.example.webstore.model.Category;
import com.example.webstore.model.CategoryEnum;
import com.example.webstore.dto.CategoryDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CategoryService {

    @Inject
    EntityManager em;

    public List<CategoryDTO> getAllCategories() {
        return em.createQuery("SELECT c FROM Category c", Category.class)
                .getResultList()
                .stream()
                .map(c -> new CategoryDTO(c.id, c.name.name()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void createCategory(String name) {
        Category category = new Category();
        category.name = CategoryEnum.valueOf(name.toUpperCase());
        em.persist(category);
    }
}
