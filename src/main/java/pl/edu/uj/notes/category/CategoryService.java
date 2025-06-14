package pl.edu.uj.notes.category;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.uj.notes.category.exception.CategoryNotFoundException;
import pl.edu.uj.notes.note.Note;
import pl.edu.uj.notes.note.NoteDTO;
import pl.edu.uj.notes.note.NoteService;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final NoteService noteService;

  String createCategory(CreateCategoryRequest request) {
    Category category = new Category(request.name());
    categoryRepository.save(category);
    return category.getId();
  }

  List<CategoryDTO> getCategories() {
    List<Category> categories = categoryRepository.findAll();
    List<CategoryDTO> categoriesDTO = new ArrayList<>();
    for (Category category : categories) {
      List<NoteDTO> noteDTOs = noteService.getNoteDTOs(category.getNotes());
      categoriesDTO.add(new CategoryDTO(category.getId(), category.getName(), noteDTOs));
    }
    return categoriesDTO;
  }

  CategoryDTO getCategory(String id) {
    Category category = categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new);
    List<NoteDTO> noteDTOs = noteService.getNoteDTOs(category.getNotes());

    return new CategoryDTO(category.getId(), category.getName(), noteDTOs);
  }

  CategoryDTO updateCategory(String id, UpdateCategoryRequest request) {
    Category category = categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new);

    List<String> noteIds = request.noteIds();
    List<Note> notes = noteService.getNotes(noteIds);

    category.setName(request.name());
    category.setNotes(notes);
    categoryRepository.save(category);
    return new CategoryDTO(category.getId(), category.getName(), noteService.getNoteDTOs(notes));
  }

  void deleteCategory(String id) {
    Category category = categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new);
    categoryRepository.delete(category);
  }
}
