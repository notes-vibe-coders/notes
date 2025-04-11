package pl.edu.uj.notes.authorization;

public class AuthorizationForUnknownUserException extends RuntimeException {
  public AuthorizationForUnknownUserException(String message) {
    super(message);
  }
}
