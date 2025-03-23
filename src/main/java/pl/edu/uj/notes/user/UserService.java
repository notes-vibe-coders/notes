package pl.edu.uj.notes.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.uj.notes.user.exceptions.UserAlreadyExistsException;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  public int createUser(CreateUserRequest request) {
    UserEntity user = new UserEntity(request.getUsername(), request.getPassword());

    if (userRepository.existsByUsername(request.getUsername())) {
      String message = String.format("User '%s' already exists", request.getUsername());
      throw new UserAlreadyExistsException(message);
    }

    userRepository.save(user);
    return user.getId();
  }

  public void updateUser(int id, UpdateUserRequest request) {
    UserEntity user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
      boolean usernameExists = userRepository.existsByUsername(request.getUsername());
      if (usernameExists) {
        throw new IllegalArgumentException("Username already taken");
      }
      user.setUsername(request.getUsername());
    }

    if (request.getPassword() != null) {
      user.setPassword(request.getPassword());
    }

    userRepository.save(user);
  }

}
