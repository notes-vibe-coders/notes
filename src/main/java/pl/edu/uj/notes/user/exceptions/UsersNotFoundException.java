package pl.edu.uj.notes.user.exceptions;

public class UsersNotFoundException extends IllegalArgumentException {
  public UsersNotFoundException(String message) {
    super(message);
  }
}
