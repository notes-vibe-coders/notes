package pl.edu.uj.notes.user;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.uj.notes.authorization.AccessControlService;
import pl.edu.uj.notes.authorization.Action;
import pl.edu.uj.notes.user.exception.InvalidOldPasswordException;
import pl.edu.uj.notes.user.exception.UnauthorizedUserAccessException;
import pl.edu.uj.notes.user.exception.UserAlreadyExistsException;
import pl.edu.uj.notes.user.exception.UserNotFoundException;
import pl.edu.uj.notes.user.exception.UsersNotFoundException;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AccessControlService accessControlService;

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
    var userToBeDeletedOptional = userRepository.findById(request.getId());

    if (userToBeDeletedOptional.isEmpty()) {
      throw new UserNotFoundException("User not found");
    }

    if (!accessControlService.userHasAccessTo(userToBeDeletedOptional.get(), Action.WRITE)) {
      throw new UnauthorizedUserAccessException(
          "You are not allowed to modify user " + request.getId());
    }

    userRepository.delete(userToBeDeletedOptional.get());
  }

  public void updatePassword(UpdatePasswordRequest request) {
    var userOptional = userRepository.findById(request.getUserId());
    if (userOptional.isEmpty()) {
      throw new UserNotFoundException("User not found");
    }
    UserEntity user = userOptional.get();
    if (!accessControlService.userHasAccessTo(user, Action.WRITE)) {
      throw new UnauthorizedUserAccessException(
          "You are not allowed to update user " + request.getUserId());
    }
    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
      throw new InvalidOldPasswordException("Old password is incorrect");
    }
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }

  public List<String> viewUsers(ViewUsersRequest request) {
    List<UserEntity> users = userRepository.findAllById(request.getIdList());
    if (users.isEmpty()) {
      String message = String.format("Users '%s' not found", request.getIdList());
      throw new UsersNotFoundException(message);
    }
    return getUsernames(users);
  }

  private static List<String> getUsernames(List<UserEntity> users) {
    return users.stream().map(UserEntity::getUsername).collect(Collectors.toList());
  }
}
