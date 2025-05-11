package pl.edu.uj.notes.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/*
TODO:
An alternative might be 409, please see:
https://stackoverflow.com/questions/3825990/http-response-code-for-post-when-resource-already-exists
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserAlreadyExistsException extends IllegalArgumentException {
  public UserAlreadyExistsException(String message) {
    super(message);
  }
}
