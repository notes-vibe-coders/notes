package pl.edu.uj.notes.note.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedNoteAccessException extends RuntimeException {
  public UnauthorizedNoteAccessException() {
    super("You do not have permission to access this note");
  }
}
