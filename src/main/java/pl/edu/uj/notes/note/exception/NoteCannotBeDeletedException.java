package pl.edu.uj.notes.note.exception;

public class NoteCannotBeDeletedException extends RuntimeException {
  public NoteCannotBeDeletedException(String message) {
    super(message);
  }
}
