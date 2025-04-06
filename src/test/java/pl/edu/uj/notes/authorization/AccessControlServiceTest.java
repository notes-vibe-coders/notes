package pl.edu.uj.notes.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.edu.uj.notes.user.InternalUserService;
import pl.edu.uj.notes.user.UserEntity;

@ExtendWith(MockitoExtension.class)
class AccessControlServiceTest {
  static final String TEST_USERNAME = "username";
  static final UserEntity TEST_USER = new UserEntity().withId("1");
  static final UserEntity TEST_OTHER_USER = new UserEntity().withId("2");

  @Mock InternalUserService userService;
  @Mock UserEntityAuthorizationStrategy userEntityAuthorizationStrategy;
  @Mock SecurityContext securityContext;
  @Mock Authentication authentication;
  @InjectMocks AccessControlService underTest;

  @Test
  void nullResource_throwsException() {
    assertThatCode(() -> underTest.userHasAccessTo(null, Action.READ))
        .isExactlyInstanceOf(NullPointerException.class);
  }

  @Test
  void nullAction_throwsException() {
    assertThatCode(() -> underTest.userHasAccessTo(TEST_USER, null))
        .isExactlyInstanceOf(NullPointerException.class);
  }

  @Test
  void subjectNotLoggedIn_throwsException() {
    assertThatCode(() -> underTest.userHasAccessTo(TEST_USER, Action.READ))
        .isExactlyInstanceOf(AuthorizationForUnknownUserException.class);
  }

  @Test
  void subjectNotFoundInDb_throwsException() {
    SecurityContextHolder.setContext(securityContext);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(TEST_USERNAME);
    when(userService.getUserByUsername(any())).thenReturn(Optional.empty());

    assertThatCode(() -> underTest.userHasAccessTo(TEST_USER, Action.READ))
        .isExactlyInstanceOf(AuthorizationForUnknownUserException.class);

    verify(userService).getUserByUsername(TEST_USERNAME);
  }

  @Nested
  class userAuthorization {

    @ParameterizedTest
    @CsvSource(
        textBlock =
            """
            READ,true
            READ,false
            WRITE,true
            WRITE,false
            """)
    void usesUserEntityAuthorizationStrategy(Action action, Boolean outcome) {
      SecurityContextHolder.setContext(securityContext);

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn(TEST_USERNAME);
      when(userService.getUserByUsername(any())).thenReturn(Optional.of(TEST_USER));

      when(userEntityAuthorizationStrategy.hasAccessTo(any(), any(), any())).thenReturn(outcome);

      var hasAccess = underTest.userHasAccessTo(TEST_OTHER_USER, action);

      assertThat(hasAccess).isEqualTo(outcome);

      verify(userEntityAuthorizationStrategy).hasAccessTo(TEST_USER, TEST_OTHER_USER, action);
    }
  }
}
