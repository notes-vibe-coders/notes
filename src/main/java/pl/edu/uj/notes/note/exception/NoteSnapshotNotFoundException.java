package pl.edu.uj.notes.note.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoteSnapshotNotFoundException extends IllegalArgumentException {
  public NoteSnapshotNotFoundException(String message) {
    super(message);
  }
}
