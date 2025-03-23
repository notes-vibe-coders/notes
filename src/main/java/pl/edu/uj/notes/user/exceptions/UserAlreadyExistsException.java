package pl.edu.uj.notes.user.exceptions;

public class UserAlreadyExistsException extends IllegalArgumentException {
  public UserAlreadyExistsException(String message) {
    super(message);
  }
}
