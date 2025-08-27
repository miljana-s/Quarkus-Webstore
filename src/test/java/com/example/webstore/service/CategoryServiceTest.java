package com.example.webstore.service;

import com.example.webstore.dto.CategoryDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
class CategoryServiceTest {

    @Inject
    CategoryService categoryService;

    @Test
    void getAllCategories_returnsAllEnumValues() {
        List<CategoryDTO> all = categoryService.getAllCategories();

        assertThat(all.size()).isGreaterThanOrEqualTo(6);

        List<String> names = all.stream().map(c -> c.name).toList();

        assertThat(names).contains(
                "NECKLACES", "BRACELETS", "EARRINGS",
                "RINGS", "PENDANTS", "ANKLETS"
        );
    }

    @Test
    void createCategory_withInvalidName_throwsIAE() {
        assertThatThrownBy(() -> categoryService.createCategory("not-a-valid-enum"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
