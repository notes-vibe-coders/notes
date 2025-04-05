package pl.edu.uj.notes.note;

import java.time.Instant;

public record NoteDTO(
    String id, String title, String content, Instant createdAt, Instant updatedAt) {}
