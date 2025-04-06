package pl.edu.uj.notes.authorization;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import pl.edu.uj.notes.user.UserEntity;

@Log4j2
@Component
final class UserEntityAuthorizationStrategy implements AuthorizationStrategy<UserEntity> {

  @Override
  public boolean hasAccessTo(UserEntity subject, UserEntity userResource, Action action) {
    if (subject.isAdmin()) {
      return true;
    }

    return StringUtils.equals(subject.getId(), userResource.getId());
  }
}
