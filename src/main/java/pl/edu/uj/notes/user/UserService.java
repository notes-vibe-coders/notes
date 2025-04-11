package pl.edu.uj.notes.user;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.uj.notes.user.exceptions.InvalidOldPasswordException;
import pl.edu.uj.notes.user.exceptions.UserAlreadyExistsException;
import pl.edu.uj.notes.user.exceptions.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public String createUser(CreateUserRequest request) {
    UserEntity user =
        new UserEntity(request.getUsername(), passwordEncoder.encode(request.getPassword()));

    if (userRepository.existsByUsername(request.getUsername())) {
      String message = String.format("User '%s' already exists", request.getUsername());
      throw new UserAlreadyExistsException(message);
    }

    return userRepository.save(user).getId();
  }

  public void deleteUser(DeleteUserRequest request) {
    String id = request.getId();

    UserEntity user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " does not exist"));

    userRepository.delete(user);
  }

  public Optional<UserEntity> getUserByUsername(String username) {
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("Username should not be null or empty");
    }

    return userRepository.getUserEntityByUsername(username);
  }

  public void updatePassword(UpdatePasswordRequest request) {
    UserEntity user =
        userRepository
            .findById(request.getUserId())
            .orElseThrow(() -> new UserNotFoundException("User not found"));

    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
      throw new InvalidOldPasswordException(
          "Old password is invalid. Please provide the correct old password.");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }
}
