package pl.edu.uj.notes.note;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface NoteSnapshotRepository extends ListCrudRepository<NoteSnapshot, String> {
  Optional<NoteSnapshot> findFirstByNoteIdOrderByCreatedAtDesc(Note note);
}
