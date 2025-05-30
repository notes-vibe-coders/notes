package pl.edu.uj.notes.note;

import jakarta.transaction.Transactional;
import java.util.*;
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

  NoteDTO getNote(String id) {
    Note note =
        noteRepository
            .findById(id)
            .orElseThrow(() -> new NoteNotFoundException("Note not found: " + id));
    NoteSnapshot recentMostSnapshot = getNoteSnapshot(note);

    return new NoteDTO(
        note.getId(),
        note.getTitle(),
        recentMostSnapshot.getContent(),
        note.getCreatedAt(),
        note.getUpdatedAt());
  }

  private NoteSnapshot getNoteSnapshot(Note note) {
    return noteSnapshotRepository
        .findFirstByNoteIdOrderByCreatedAtDesc(note)
        .orElseThrow(
            () ->
                new NoteSnapshotNotFoundException(
                    "Note snapshot not found for note: " + note.getId()));
  }

  List<NoteDTO> getAllNotes(String title, String content) {
    title = title == null ? "" : title;
    content = content == null ? "" : content;

    List<Note> notes = noteRepository.findAllByTitleContainingIgnoreCase(title);

    Map<Note, NoteSnapshot> noteSnapshotMap = new HashMap<>();
    for (Note note : notes) {
      NoteSnapshot noteSnapshot = getNoteSnapshot(note);
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

  public List<NoteDTO> getNoteDTOs(List<Note> notes) {
    List<NoteDTO> noteDTOs = new ArrayList<>();
    for (Note note : notes) {
      NoteSnapshot noteSnapshot = getNoteSnapshot(note);
      noteDTOs.add(
          new NoteDTO(
              note.getId(),
              note.getTitle(),
              noteSnapshot.getContent(),
              note.getCreatedAt(),
              note.getUpdatedAt()));
    }
    return noteDTOs;
  }

  public List<Note> getNotes(List<String> noteIds) {
    return noteRepository.findAllById(noteIds);
  }
}
