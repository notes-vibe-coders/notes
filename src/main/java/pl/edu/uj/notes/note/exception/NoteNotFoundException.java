package pl.edu.uj.notes.note.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoteNotFoundException extends IllegalArgumentException {
  public NoteNotFoundException() {
    super("Note not found");
  }
}
