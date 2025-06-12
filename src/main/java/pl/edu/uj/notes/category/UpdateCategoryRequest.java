package pl.edu.uj.notes.category;

import java.util.List;

record UpdateCategoryRequest(String name, List<String> noteIds) {}
