package pl.edu.uj.notes.authorization;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.uj.notes.authentication.PrincipalService;
import pl.edu.uj.notes.user.UserEntity;

@Service
@RequiredArgsConstructor
public class AccessControlService {

  private final PrincipalService principalService;
  private final UserEntityAuthorizationStrategy userEntityAuthorizationStrategy;

  public boolean userHasAccessTo(@NonNull Object resource, @NonNull Action action) {
    UserEntity subject = principalService.fetchCurrentUser();

    if (resource instanceof UserEntity userEntity) {
      return userEntityAuthorizationStrategy.hasAccessTo(subject, userEntity, action);
    }

    return false;
  }
}
