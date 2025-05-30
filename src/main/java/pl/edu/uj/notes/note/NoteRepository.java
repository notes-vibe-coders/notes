package pl.edu.uj.notes.note;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, String> {
  List<Note> findAllByTitleContainingIgnoreCase(String title);
}
