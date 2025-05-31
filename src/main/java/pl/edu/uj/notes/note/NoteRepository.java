package pl.edu.uj.notes.note;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface NoteRepository extends JpaRepository<Note, String> {
  List<Note> findAllByTitleContainingIgnoreCaseAndActive(String title, boolean active);

  Optional<Note> findByActiveAndId(boolean active, String id);
}
