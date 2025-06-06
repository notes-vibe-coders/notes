package pl.edu.uj.notes.authentication;

import pl.edu.uj.notes.user.UserEntity;

public interface PrincipalService {
  UserEntity fetchCurrentUser();
}
