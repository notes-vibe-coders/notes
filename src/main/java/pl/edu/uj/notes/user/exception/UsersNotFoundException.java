package pl.edu.uj.notes.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UsersNotFoundException extends IllegalArgumentException {
  public UsersNotFoundException(String message) {
    super(message);
  }
}
