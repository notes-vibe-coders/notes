package pl.edu.uj.notes.category;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.uj.notes.authentication.SecurityConfig;
import pl.edu.uj.notes.category.exception.CategoryNotFoundException;
import pl.edu.uj.notes.note.NoteService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(SecurityConfig.class)
public class CategoryServiceTest {

  @Autowired private CategoryService categoryService;

  @MockitoBean private CategoryRepository categoryRepository;
  @MockitoBean private NoteService noteService;

  private static final String CATEGORY_ID = "category-1";
  private static final String CATEGORY_NAME = "Books";

  @Nested
  class CreateCategory {

    @Test
    void whenValidRequest_thenReturnsCategoryId() {
      // Given
      CreateCategoryRequest request = new CreateCategoryRequest(CATEGORY_NAME);
      Category category = new Category(CATEGORY_NAME);
      when(categoryRepository.save(any()))
          .thenAnswer(
              inv -> {
                Category c = inv.getArgument(0);
                c.setId(CATEGORY_ID);
                return c;
              });

      // When
      String result = categoryService.createCategory(request);

      // Then
      assertEquals(CATEGORY_ID, result);
    }
  }

  @Nested
  class GetCategories {

    @Test
    void whenCategoriesExist_thenReturnList() {
      // Given
      Category category = new Category(CATEGORY_NAME);
      category.setId(CATEGORY_ID);
      when(categoryRepository.findAll()).thenReturn(List.of(category));
      when(noteService.getNoteDTOs(any())).thenReturn(List.of());

      // When
      List<CategoryDTO> result = categoryService.getCategories();

      // Then
      assertEquals(1, result.size());
      assertEquals(CATEGORY_ID, result.get(0).id());
    }
  }

  @Nested
  class GetCategory {

    @Test
    void whenCategoryExists_thenReturnDTO() {
      // Given
      Category category = new Category(CATEGORY_NAME);
      category.setId(CATEGORY_ID);
      when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
      when(noteService.getNoteDTOs(any())).thenReturn(List.of());

      // When
      CategoryDTO dto = categoryService.getCategory(CATEGORY_ID);

      // Then
      assertEquals(CATEGORY_ID, dto.id());
      assertEquals(CATEGORY_NAME, dto.name());
    }

    @Test
    void whenCategoryNotFound_thenThrowException() {
      // Given
      when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

      // When & Then
      assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategory(CATEGORY_ID));
    }
  }

  @Nested
  class DeleteCategory {

    @Test
    void whenCategoryExists_thenDelete() {
      // Given
      Category category = new Category(CATEGORY_NAME);
      category.setId(CATEGORY_ID);
      when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
      doNothing().when(categoryRepository).delete(category);

      // When
      categoryService.deleteCategory(CATEGORY_ID);

      // Then
      verify(categoryRepository).delete(category);
    }

    @Test
    void whenCategoryNotFound_thenThrowException() {
      // Given
      when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

      // When & Then
      assertThrows(
          CategoryNotFoundException.class, () -> categoryService.deleteCategory(CATEGORY_ID));
    }
  }
}
