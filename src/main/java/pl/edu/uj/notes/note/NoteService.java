package pl.edu.uj.notes.note;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import pl.edu.uj.notes.authentication.PrincipalService;
import pl.edu.uj.notes.note.exception.NoteIsArchivizedException;
import pl.edu.uj.notes.note.exception.NoteNotFoundException;
import pl.edu.uj.notes.note.exception.NoteSnapshotNotFoundException;

@Component
@RequiredArgsConstructor
public class NoteService {

  private final PrincipalService principalService;
  private final NoteRepository noteRepository;
  private final NoteSnapshotRepository noteSnapshotRepository;

  @Transactional
  String createNote(CreateNoteRequest request) {
    Note note = new Note(request.title(), principalService.fetchCurrentUser());
    note = noteRepository.save(note);

    NoteSnapshot noteSnapshot = new NoteSnapshot(note, request.content());
    noteSnapshotRepository.save(noteSnapshot);
    return note.getId();
  }

  void deleteNote(DeleteNoteRequest request) {
    String id = request.id();

    Optional<Note> noteOptional = noteRepository.findById(id);

    if (noteOptional.isEmpty()) {
      throw new NoteNotFoundException("Note with ID " + id + " does not exist");
    } else if (noteOptional.get().isArchived()) {
      throw new NoteIsArchivizedException("Note with ID " + id + " is archivized");
    } else {
      Note note = noteOptional.get();
      note.setActive(false);
      noteRepository.save(note);
    }
  }

  public void markAsImportant(String id) {
    Note note =
        noteRepository
            .findById(id)
            .orElseThrow(() -> new NoteNotFoundException("Note not found: " + id));
    if (note.isArchived()) {
      throw new NoteIsArchivizedException("Note with ID " + id + " is archivized");
    }
    note.setImportant(true);
    noteRepository.save(note);
  }

  public void markAsArchived(String id) {
    Note note =
        noteRepository
            .findById(id)
            .orElseThrow(() -> new NoteNotFoundException("Note not found: " + id));
    note.setArchived(true);
    noteRepository.save(note);
  }

  NoteDTO getNote(String id) {
    Note note =
        noteRepository
            .findByActiveAndId(true, id)
            .orElseThrow(() -> new NoteNotFoundException("Note not found: " + id));

    if (note.isArchived()) {
      throw new NoteIsArchivizedException("Note with ID " + id + " is archivized");
    }

    NoteSnapshot recentMostSnapshot = getNoteSnapshot(note);

    return new NoteDTO(
        note.getId(),
        note.getTitle(),
        recentMostSnapshot.getContent(),
        note.getCreatedAt(),
        note.getUpdatedAt(),
        note.isImportant(),
        note.isArchived());
  }

  private NoteSnapshot getNoteSnapshot(Note note) {
    if (note.isArchived()) {
      throw new NoteIsArchivizedException("Note with ID " + note.getId() + " is archivized");
    }
    return noteSnapshotRepository
        .findFirstByNoteIdOrderByCreatedAtDesc(note)
        .orElseThrow(
            () ->
                new NoteSnapshotNotFoundException(
                    "Note snapshot not found for note: " + note.getId()));
  }

  public List<NoteDTO> getAllNotes(String title, String content, Boolean important) {
    title = title == null ? "" : title;
    content = content == null ? "" : content;
    important = important != null && important;

    List<Note> notes = noteRepository.findAllByTitleContainingIgnoreCaseAndActive(title, true);

    if (notes.stream().anyMatch(Note::isArchived)) {
      throw new NoteIsArchivizedException("At least one note is archivized");
    }

    if (important) {
      notes = notes.stream().filter(Note::isImportant).toList();
    }

    Map<Note, NoteSnapshot> noteSnapshotMap = new HashMap<>();
    for (Note note : notes) {
      NoteSnapshot noteSnapshot =
          noteSnapshotRepository
              .findFirstByNoteIdOrderByCreatedAtDesc(note)
              .orElseThrow(
                  () ->
                      new NoteSnapshotNotFoundException(
                          "Note snapshot not found for note: " + note.getId()));
      if (noteSnapshot.getContent().toLowerCase().contains(content.toLowerCase())) {
        noteSnapshotMap.put(note, noteSnapshot);
      }
    }

    return noteSnapshotMap.entrySet().stream()
        .map(
            entry ->
                new NoteDTO(
                    entry.getKey().getId(),
                    entry.getKey().getTitle(),
                    entry.getValue().getContent(),
                    entry.getKey().getCreatedAt(),
                    entry.getKey().getUpdatedAt(),
                    entry.getKey().isImportant(),
                    entry.getKey().isArchived()))
        .toList();
  }

  NoteDTO updateNote(String id, CreateNoteRequest request) {
    Optional<Note> currentNoteOptional = noteRepository.findById(id);
    if (currentNoteOptional.isEmpty() || !currentNoteOptional.get().isActive()) {
      throw new NoteNotFoundException("Note not found");
    }
    if (currentNoteOptional.get().isArchived()) {
      throw new NoteIsArchivizedException("Note with ID " + id + " is archivized");
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

  public List<NoteDTO> getNoteDTOs(List<Note> notes) {
    List<NoteDTO> noteDTOs = new ArrayList<>();
    for (Note note : notes) {
      if (note.isArchived()) {
        throw new NoteIsArchivizedException("Note with ID " + note.getId() + " is archivized");
      }
      NoteSnapshot noteSnapshot = getNoteSnapshot(note);
      noteDTOs.add(
          new NoteDTO(
              note.getId(),
              note.getTitle(),
              noteSnapshot.getContent(),
              note.getCreatedAt(),
              note.getUpdatedAt(),
              note.isImportant(),
              note.isArchived()));
    }
    return noteDTOs;
  }

  public List<Note> getNotes(List<String> noteIds) {
    return noteRepository.findAllById(noteIds).stream()
        .peek(
            note -> {
              if (note.isArchived()) {
                throw new NoteIsArchivizedException(
                    "Note with ID " + note.getId() + " is archivized");
              }
            })
        .toList();
  }
}
