package pl.edu.uj.notes.note;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;

public record NoteDTO(
    String id,
    String title,
    String content,
    Instant createdAt,
    Instant updatedAt,
    boolean important,
    boolean archivized) {

  public NoteDTO(Note note, NoteSnapshot snapshot) {
    this(
        note.getId(),
        note.getTitle(),
        snapshot.getContent(),
        note.getCreatedAt(),
        getUpdatedAt(note, snapshot),
        note.isImportant(),
        note.isArchived());
  }

  @JsonIgnore
  private static Instant getUpdatedAt(Note note, NoteSnapshot noteSnapshot) {
    if (note.getUpdatedAt().isAfter(noteSnapshot.getUpdatedAt())) {
      return note.getUpdatedAt();
    }

    return noteSnapshot.getUpdatedAt();
  }
}
