package pl.edu.uj.notes.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.uj.notes.user.api.CreateUserRequest;
import pl.edu.uj.notes.user.api.ViewUsersRequest;
import pl.edu.uj.notes.user.exceptions.UserAlreadyExistsException;
import pl.edu.uj.notes.user.exceptions.UsersNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

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
