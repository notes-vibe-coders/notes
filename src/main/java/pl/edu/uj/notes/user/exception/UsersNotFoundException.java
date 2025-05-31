package pl.edu.uj.notes.user.exception;

public class UsersNotFoundException extends IllegalArgumentException {
  public UsersNotFoundException(String message) {
    super(message);
  }
}
