package pl.edu.uj.notes.note;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface NoteSnapshotRepository extends ListCrudRepository<NoteSnapshot, String> {}
