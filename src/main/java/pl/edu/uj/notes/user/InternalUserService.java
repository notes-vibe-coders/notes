package pl.edu.uj.notes.user;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternalUserService {

  private final UserRepository userRepository;

  /*
  TODO: please run test coverage - if I see correctly this method is effectively not tested
   */
  public Optional<UserEntity> getUserByUsername(String username) {
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("Username should not be null or empty");
    }

    return userRepository.getUserEntityByUsername(username);
  }
}
