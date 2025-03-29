package pl.edu.uj.notes.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.uj.notes.user.exceptions.UserAlreadyExistsException;
import pl.edu.uj.notes.user.exceptions.UserNotFoundException;

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

  public void deleteUser(int id) {
    UserEntity user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " does not exist"));
    userRepository.delete(user);
  }
}
