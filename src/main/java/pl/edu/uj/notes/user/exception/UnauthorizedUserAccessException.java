package pl.edu.uj.notes.user.exception;

public class UnauthorizedUserAccessException extends RuntimeException {
  public UnauthorizedUserAccessException(String message) {
    super(message);
  }
}
