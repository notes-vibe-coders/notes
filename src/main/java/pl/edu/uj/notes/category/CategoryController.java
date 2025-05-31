package pl.edu.uj.notes.category;

import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/categories")
class CategoryController {

  private final CategoryService categoryService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  ResponseEntity<Void> createCategory(@RequestBody CreateCategoryRequest request) {
    String id = categoryService.createCategory(request);
    URI location = URI.create("api/v1/categories/" + id);
    return ResponseEntity.created(location).build();
  }

  @GetMapping
  ResponseEntity<List<CategoryDTO>> getCategories() {
    return ResponseEntity.ok(categoryService.getCategories());
  }

  @GetMapping("/{id}")
  ResponseEntity<CategoryDTO> getCategory(@PathVariable String id) {
    return ResponseEntity.ok(categoryService.getCategory(id));
  }

  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  ResponseEntity<Category> updateCategory(@RequestBody UpdateCategoryRequest request) {
    return ResponseEntity.ok(categoryService.updateCategory(request));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  ResponseEntity<Void> deleteCategory(@PathVariable String id) {
    categoryService.deleteCategory(id);
    return ResponseEntity.noContent().build();
  }
}
