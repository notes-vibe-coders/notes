package pl.edu.uj.notes.authentication;

public class NoUserForAuthenticatedPrincipalException extends RuntimeException {
  public NoUserForAuthenticatedPrincipalException(String message) {
    super(message);
  }
}
