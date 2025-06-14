package pl.edu.uj.notes.authentication;

public class NotAuthenticatedException extends RuntimeException {
  public NotAuthenticatedException(String message) {
    super(message);
  }
}
