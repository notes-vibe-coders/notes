package pl.edu.uj.notes.note.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NoteAccessDeniedException extends RuntimeException {
  public NoteAccessDeniedException(String message) {
    super(message);
  }
}
