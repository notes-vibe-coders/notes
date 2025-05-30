package pl.edu.uj.notes.category;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

record UpdateCategoryRequest(@NotBlank String id, String name, List<String> noteIds) {}
