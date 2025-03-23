package pl.edu.uj.notes.user.exceptions;

public class UserNotFoundException extends IllegalArgumentException {
  public UserNotFoundException(String message) {
    super(message);
  }
}
