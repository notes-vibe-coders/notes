package pl.edu.uj.notes.authorization;

import pl.edu.uj.notes.user.UserEntity;

public interface AuthorizationStrategy<T> {

  boolean hasAccessTo(UserEntity subject, T resource, Action action);
}
