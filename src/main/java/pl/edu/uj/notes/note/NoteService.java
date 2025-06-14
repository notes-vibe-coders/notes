package pl.edu.uj.notes.note;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import pl.edu.uj.notes.authentication.PrincipalService;
import pl.edu.uj.notes.authorization.AccessControlService;
import pl.edu.uj.notes.authorization.Action;
import pl.edu.uj.notes.note.exception.NoteNotFoundException;
import pl.edu.uj.notes.note.exception.NoteSnapshotNotFoundException;
import pl.edu.uj.notes.note.exception.UnauthorizedNoteAccessException;

@Component
@RequiredArgsConstructor
public class NoteService {

  private final PrincipalService principalService;
  private final NoteRepository noteRepository;
  private final NoteSnapshotRepository noteSnapshotRepository;
  private final AccessControlService accessControlService;

  @Transactional
  String createNote(@NonNull CreateNoteRequest request) {
    Note note = new Note(request.title(), principalService.fetchCurrentUser());
    note = noteRepository.save(note);

    NoteSnapshot noteSnapshot = new NoteSnapshot(note, request.content());
    noteSnapshotRepository.save(noteSnapshot);
    return note.getId();
  }

  NoteDTO updateNote(@NonNull String id, @NonNull CreateNoteRequest request) {
    Note note = getNoteWithAccessControl(id, Action.WRITE);

    if (!StringUtils.equals(note.getTitle(), request.title())) {
      note.setTitle(request.title());
      note = noteRepository.save(note);
    }

    NoteSnapshot latestSnapshot = latestSnapshot(note);
    if (!latestSnapshot.getContent().equals(request.content())) {
      NoteSnapshot newSnapshot = new NoteSnapshot(note, request.content());
      latestSnapshot = noteSnapshotRepository.save(newSnapshot);
    }

    return new NoteDTO(note, latestSnapshot);
  }

  void deleteNote(@NonNull DeleteNoteRequest request) {
    Note note = getNoteWithAccessControl(request.id(), Action.WRITE);
    note.setActive(false);
    noteRepository.save(note);
  }

  public void markAsImportant(@NonNull String id) {
    Note note = getNoteWithAccessControl(id, Action.WRITE);

    note.setImportant(true);
    noteRepository.save(note);
  }

  NoteDTO getNote(@NonNull String id) {
    Note note = getNoteWithAccessControl(id, Action.READ);

    return new NoteDTO(note, latestSnapshot(note));
  }

  List<NoteDTO> getAllNotes(String title, String content, Boolean important) {
    title = title == null ? "" : title;
    content = content == null ? "" : content;
    important = important != null && important;

    List<Note> notes =
        noteRepository.findAllByTitleContainingIgnoreCaseAndActiveIsTrue(title).stream()
            .filter(note -> accessControlService.userHasAccessTo(note, Action.READ))
            .toList();

    if (important) {
      notes = notes.stream().filter(Note::isImportant).toList();
    }

    Map<Note, NoteSnapshot> noteSnapshotMap = new HashMap<>();
    for (Note note : notes) {
      NoteSnapshot snapshot = latestSnapshot(note);
      if (snapshot.getContent().toLowerCase().contains(content.toLowerCase())) {
        noteSnapshotMap.put(note, snapshot);
      }
    }

    return noteSnapshotMap.entrySet().stream()
        .map(entry -> new NoteDTO(entry.getKey(), entry.getValue()))
        .toList();
  }

  public List<NoteDTO> getNoteDTOs(@NonNull List<Note> notes) {
    List<NoteDTO> noteDTOs = new ArrayList<>();
    for (Note note : notes) {
      NoteSnapshot noteSnapshot = latestSnapshot(note);
      noteDTOs.add(
          new NoteDTO(
              note.getId(),
              note.getTitle(),
              noteSnapshot.getContent(),
              note.getCreatedAt(),
              note.getUpdatedAt(),
              note.isImportant()));
    }
    return noteDTOs;
  }

  public List<Note> getNotes(@NonNull List<String> noteIds) {
    return noteRepository.findAllById(noteIds).stream()
        .filter(note -> accessControlService.userHasAccessTo(note, Action.READ))
        .toList();
  }

  private Note getNoteWithAccessControl(String id, Action action) {
    var note = noteRepository.findByIdAndActiveIsTrue(id).orElseThrow(NoteNotFoundException::new);
    if (!accessControlService.userHasAccessTo(note, action)) {
      throw new UnauthorizedNoteAccessException();
    }

    return note;
  }

  private NoteSnapshot latestSnapshot(Note note) {
    return noteSnapshotRepository
        .findFirstByNoteIdOrderByCreatedAtDesc(note)
        .orElseThrow(NoteSnapshotNotFoundException::new);
  }
}
