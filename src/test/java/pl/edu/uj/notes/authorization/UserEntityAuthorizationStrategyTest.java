package pl.edu.uj.notes.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import pl.edu.uj.notes.user.UserEntity;

class UserEntityAuthorizationStrategyTest {
  static final String SUBJECT_ID = "subjectID";
  static final String OTHER_USER_ID = "otherUserID";

  AuthorizationStrategy<UserEntity> underTest = new UserEntityAuthorizationStrategy();

  @ParameterizedTest
  @EnumSource(Action.class)
  void sameUserIsSubjectAndResource_true(Action action) {
    var subject = new UserEntity().withId(SUBJECT_ID);

    var hasAccess = underTest.hasAccessTo(subject, subject, action);

    assertThat(hasAccess).isTrue();
  }

  @ParameterizedTest
  @EnumSource(Action.class)
  void notAdminTryingToAccessOtherUser_false(Action action) {
    var subject = new UserEntity().withId(SUBJECT_ID);
    var otherUser = new UserEntity().withId(OTHER_USER_ID);

    var hasAccess = underTest.hasAccessTo(subject, otherUser, action);

    assertThat(hasAccess).isFalse();
  }

  @ParameterizedTest
  @EnumSource(Action.class)
  void adminTryingToAccessOtherUser_true(Action action) {
    var subject = new UserEntity().withId(SUBJECT_ID).withAdmin(true);
    var otherUser = new UserEntity().withId(OTHER_USER_ID);

    var hasAccess = underTest.hasAccessTo(subject, otherUser, action);

    assertThat(hasAccess).isTrue();
  }
}
