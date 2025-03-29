package pl.edu.uj.notes.note;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoteService {

  private final NoteRepository noteRepository;
  private final NoteSnapshotRepository noteSnapshotRepository;

  @Transactional
  String createNote(CreateNoteRequest request) {
    Note note = new Note(request.title());
    note = noteRepository.save(note);

    NoteSnapshot noteSnapshot = new NoteSnapshot(note, request.content());
    return noteSnapshotRepository.save(noteSnapshot).getId();
  }
}
