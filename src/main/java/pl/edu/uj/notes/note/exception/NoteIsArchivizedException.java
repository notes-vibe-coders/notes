package pl.edu.uj.notes.note.exception;

public class NoteIsArchivizedException extends RuntimeException {
  public NoteIsArchivizedException(String message) {
    super(message);
  }
}
