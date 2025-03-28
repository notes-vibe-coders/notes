package pl.edu.uj.notes.user;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.uj.notes.user.exceptions.UserAlreadyExistsException;
import pl.edu.uj.notes.user.exceptions.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public int createUser(CreateUserRequest request) {
    UserEntity user =
        new UserEntity(request.getUsername(), passwordEncoder.encode(request.getPassword()));

    if (userRepository.existsByUsername(request.getUsername())) {
      String message = String.format("User '%s' already exists", request.getUsername());
      throw new UserAlreadyExistsException(message);
    }

    userRepository.save(user);
    return user.getId();
  }

  public Optional<UserEntity> getUserByUsername(String username) {
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("Username should not be null or empty");
    }

    return userRepository.getUserEntityByUsername(username);
  }

  public void updateUser(int id, UpdateUserRequest request) {
    UserEntity user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

    if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
      boolean usernameExists = userRepository.existsByUsername(request.getUsername());
      if (usernameExists) {
        throw new UserAlreadyExistsException("User with this username already exists");
      }
      user.setUsername(request.getUsername());
    }

    if (request.getPassword() != null) {
      user.setPassword(request.getPassword());
    }

    userRepository.save(user);
  }
}
