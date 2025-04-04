package pl.edu.uj.notes.note.exceptions;

public class NoteNotFoundException extends RuntimeException {
  public NoteNotFoundException(String message) {
    super(message);
  }
}
