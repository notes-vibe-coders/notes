package pl.edu.uj.notes.note.exception;

public class UnauthorizedNoteAccessException extends RuntimeException {
  public UnauthorizedNoteAccessException() {
    super("You do not have permission to access this note");
  }
}
