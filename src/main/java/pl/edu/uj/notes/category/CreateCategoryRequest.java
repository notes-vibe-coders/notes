package pl.edu.uj.notes.category;

import jakarta.validation.constraints.NotBlank;

record CreateCategoryRequest(@NotBlank String name) {}
