package pl.edu.uj.notes.note;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import pl.edu.uj.notes.note.exception.NoteNotFoundException;
import pl.edu.uj.notes.note.exception.NoteSnapshotNotFoundException;

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
    noteSnapshotRepository.save(noteSnapshot);
    return note.getId();
  }

  void deleteNote(DeleteNoteRequest request) {
    String id = request.id();

    Optional<Note> noteOptional = noteRepository.findById(id);

    if (noteOptional.isPresent()) {
      Note note = noteOptional.get();
      note.setActive(false);
      noteRepository.save(note);
    } else {
      throw new NoteNotFoundException("Note with ID " + id + " does not exist");
    }
  }

  public NoteDTO getNote(String id) {
    Note note =
        noteRepository
            .findById(id)
            .orElseThrow(() -> new NoteNotFoundException("Note not found: " + id));
    NoteSnapshot recentMostSnapshot =
        noteSnapshotRepository
            .findFirstByNoteIdOrderByCreatedAtDesc(note)
            .orElseThrow(
                () -> new NoteSnapshotNotFoundException("Note snapshot not found for note: " + id));

    return new NoteDTO(
        note.getId(),
        note.getTitle(),
        recentMostSnapshot.getContent(),
        note.getCreatedAt(),
        note.getUpdatedAt());
  }

  public List<NoteDTO> getAllNotes() {
    Map<Note, NoteSnapshot> noteSnapshotMap =
        noteRepository.findAll().stream()
            .collect(
                Collectors.toMap(
                    Function.identity(),
                    note ->
                        noteSnapshotRepository
                            .findFirstByNoteIdOrderByCreatedAtDesc(note)
                            .orElseThrow(
                                () ->
                                    new NoteSnapshotNotFoundException(
                                        "Note snapshot not found for note: " + note.getId()))));

    return noteSnapshotMap.entrySet().stream()
        .map(
            entry ->
                new NoteDTO(
                    entry.getKey().getId(),
                    entry.getKey().getTitle(),
                    entry.getValue().getContent(),
                    entry.getKey().getCreatedAt(),
                    entry.getKey().getUpdatedAt()))
        .toList();
  }

  NoteDTO updateNote(String id, CreateNoteRequest request) {
    Optional<Note> currentNoteOptional = noteRepository.findById(id);
    if (currentNoteOptional.isEmpty() || !currentNoteOptional.get().isActive()) {
      throw new NoteNotFoundException("Note not found");
    }

    Note note = currentNoteOptional.get();
    if (!StringUtils.equals(note.getTitle(), request.title())) {
      note.setTitle(request.title());
      note = noteRepository.save(note);
    }

    Optional<NoteSnapshot> latestSnapshotOptional =
        noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(note);
    NoteSnapshot latestSnapshot;

    if (latestSnapshotOptional.isEmpty()
        || !StringUtils.equals(latestSnapshotOptional.get().getContent(), request.content())) {
      NoteSnapshot newSnapshot = new NoteSnapshot(note, request.content());
      latestSnapshot = noteSnapshotRepository.save(newSnapshot);
    } else {
      latestSnapshot = latestSnapshotOptional.get();
    }

    return new NoteDTO(note, latestSnapshot);
  }
}
