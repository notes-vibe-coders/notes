package pl.edu.uj.notes.note;

import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface NoteRepository extends ListCrudRepository<Note, String> {
  List<Note> findAllByTitleContainingIgnoreCase(String title);
}
