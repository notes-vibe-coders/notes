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

    public void updateUser(int id, UpdateUserRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            userRepository.findByUsername(request.getUsername())
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Username already taken");
                    });
            user.setUsername(request.getUsername());
        }

        if (request.getPassword() != null) {
            user.setPassword(request.getPassword());
        }

        userRepository.save(user);
    }

}
