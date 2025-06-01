package pl.edu.uj.notes.category;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.edu.uj.notes.authentication.SecurityConfig;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
class CategoryControllerTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean CategoryService categoryService;

  private static final String CATEGORY_ID = "cat-1";
  private static final String CATEGORY_NAME = "Books";

  @WithMockUser
  @Test
  void whenCreateCategory_thenReturnsCreated() throws Exception {
    when(categoryService.createCategory(any())).thenReturn(CATEGORY_ID);

    String json =
        """
                {
                  "name": "Books"
                }
                """;

    mockMvc
        .perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "api/v1/categories/" + CATEGORY_ID));
  }

  @WithMockUser
  @Test
  void whenGetCategories_thenReturnsOk() throws Exception {
    CategoryDTO dto = new CategoryDTO(CATEGORY_ID, CATEGORY_NAME, List.of());
    when(categoryService.getCategories()).thenReturn(List.of(dto));

    mockMvc.perform(get("/api/v1/categories")).andExpect(status().isOk());
  }

  @WithMockUser
  @Test
  void whenGetCategoryById_thenReturnsOk() throws Exception {
    CategoryDTO dto = new CategoryDTO(CATEGORY_ID, CATEGORY_NAME, List.of());
    when(categoryService.getCategory(CATEGORY_ID)).thenReturn(dto);

    mockMvc.perform(get("/api/v1/categories/{id}", CATEGORY_ID)).andExpect(status().isOk());
  }

  @WithMockUser
  @Test
  void whenUpdateCategory_thenReturnsOk() throws Exception {
    String json =
        """
                {
                  "id": "cat-1",
                  "name": "Updated Books",
                  "noteIds": ["note-1", "note-2"]
                }
                """;

    Category category = new Category("Updated Books");
    category.setId(CATEGORY_ID);

    when(categoryService.updateCategory(any())).thenReturn(category);

    mockMvc
        .perform(put("/api/v1/categories").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isOk());
  }

  @WithMockUser
  @Test
  void whenDeleteCategory_thenReturnsNoContent() throws Exception {
    doNothing().when(categoryService).deleteCategory(CATEGORY_ID);

    mockMvc
        .perform(delete("/api/v1/categories/{id}", CATEGORY_ID))
        .andExpect(status().isNoContent());
  }
}
