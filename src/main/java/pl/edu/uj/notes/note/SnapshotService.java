package pl.edu.uj.notes.note;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.uj.notes.note.exception.NoteNotFoundException;
import pl.edu.uj.notes.note.exception.NoteSnapshotNotFoundException;

@Service
@RequiredArgsConstructor
class SnapshotService {

  private final NoteRepository noteRepository;
  private final NoteSnapshotRepository snapshotRepository;

  List<SnapshotDTO> getSnapshotsByNoteId(String noteId) {
    Optional<Note> note = noteRepository.findById(noteId);
    if (note.isEmpty()) {
      throw new NoteNotFoundException();
    }

    List<NoteSnapshot> snapshots = snapshotRepository.findAllByNoteId(note.get());

    return snapshots.stream().map(SnapshotDTO::from).toList();
  }

  SnapshotDTO restoreSnapshot(String noteId, String snapshotId) {
    Optional<Note> note = noteRepository.findById(noteId);
    if (note.isEmpty()) {
      throw new NoteNotFoundException();
    }

    Optional<NoteSnapshot> snapshot = snapshotRepository.findById(snapshotId);
    if (snapshot.isEmpty()) {
      throw new NoteSnapshotNotFoundException();
    }

    NoteSnapshot actual = snapshot.get();
    actual.setCreatedAt(Instant.now());

    return SnapshotDTO.from(snapshotRepository.save(actual));
  }
}
