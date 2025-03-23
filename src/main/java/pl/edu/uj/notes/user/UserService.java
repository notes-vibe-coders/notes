package pl.edu.uj.notes.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  public int create(CreateUserRequest request) {
    UserEntity user = new UserEntity(request.getUsername(), request.getPassword());

    userRepository
        .findByUsername(request.getUsername())
        .ifPresent(
            u -> {
              throw new IllegalArgumentException("User with this username already exists");
            });

    userRepository.save(user);
    return user.getId();
  }

  public void deleteUser(int id) {
    UserEntity user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));


    userRepository.delete(user);
  }
}
