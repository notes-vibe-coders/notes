package pl.edu.uj.notes.authorization;

import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.edu.uj.notes.user.InternalUserService;
import pl.edu.uj.notes.user.UserEntity;

@Service
@RequiredArgsConstructor
public class AccessControlService {

  private final InternalUserService userService;
  private final UserEntityAuthorizationStrategy userEntityAuthorizationStrategy;

  public boolean userHasAccessTo(@NonNull Object resource, @NonNull Action action) {
    UserEntity subject = fetchSubject();

    if (resource instanceof UserEntity userEntity) {
      return userEntityAuthorizationStrategy.hasAccessTo(subject, userEntity, action);
    }

    return false;
  }

  private UserEntity fetchSubject() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      var msg = "Authorization attempt for unknown user";
      throw new AuthorizationForUnknownUserException(msg);
    }

    Optional<UserEntity> currentUser = userService.getUserByUsername(authentication.getName());
    if (currentUser.isEmpty()) {
      var msg = "Failed to get user for " + authentication.getName();
      throw new AuthorizationForUnknownUserException(msg);
    }

    return currentUser.get();
  }
}
