package pl.edu.uj.notes.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.edu.uj.notes.user.InternalUserService;
import pl.edu.uj.notes.user.UserEntity;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  @Mock InternalUserService userService;

  @InjectMocks AuthenticationService underTest;

  @Test
  void userNotFound_throwsException() {
    when(userService.getUserByUsername(any())).thenReturn(Optional.empty());

    assertThatCode(() -> underTest.loadUserByUsername("username"))
        .isExactlyInstanceOf(UsernameNotFoundException.class);
  }

  @Test
  void userFound_returnsUserDetails() {
    var user = new UserEntity("user", "password");
    when(userService.getUserByUsername(any())).thenReturn(Optional.of(user));

    var userDetails = underTest.loadUserByUsername(user.getUsername());

    verify(userService).getUserByUsername(user.getUsername());
    verifyNoMoreInteractions(userService);
    assertThat(userDetails)
        .extracting(UserDetails::getUsername, UserDetails::getPassword)
        .containsExactly(user.getUsername(), user.getPassword());
  }

  @Nested
  class FetchCurrentUserTest {
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "password";

    private MockedStatic<SecurityContextHolder> securityContextHolderMockedStatic;
    private SecurityContext securityContext;
    private Authentication authentication;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
      securityContext = mock(SecurityContext.class);
      authentication = mock(Authentication.class);
      securityContextHolderMockedStatic = Mockito.mockStatic(SecurityContextHolder.class);
      securityContextHolderMockedStatic
          .when(SecurityContextHolder::getContext)
          .thenReturn(securityContext);
      when(securityContext.getAuthentication()).thenReturn(authentication);
      testUser = new UserEntity(USERNAME, PASSWORD);
    }

    @AfterEach
    void tearDown() {
      securityContextHolderMockedStatic.close();
    }

    @Test
    void whenNotAuthenticated_thenThrowsNotAuthenticatedException() {
      when(securityContext.getAuthentication()).thenReturn(null);

      assertThatThrownBy(() -> underTest.fetchCurrentUser())
          .isInstanceOf(NotAuthenticatedException.class)
          .hasMessage("User not authenticated");
    }

    @Test
    void whenAuthenticatedButUserNotFound_thenThrowsNoUserForAuthenticatedPrincipalException() {
      when(authentication.getName()).thenReturn(USERNAME);
      when(userService.getUserByUsername(USERNAME)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> underTest.fetchCurrentUser())
          .isInstanceOf(NoUserForAuthenticatedPrincipalException.class)
          .hasMessage("Failed to get user for " + USERNAME);
    }

    @Test
    void whenAuthenticatedAndUserFound_thenReturnsUser() {
      when(authentication.getName()).thenReturn(USERNAME);
      when(userService.getUserByUsername(USERNAME)).thenReturn(Optional.of(testUser));

      UserEntity result = underTest.fetchCurrentUser();

      assertThat(result).isSameAs(testUser);
      verify(userService).getUserByUsername(USERNAME);
    }
  }

  @Test
  void blockedUser_isDisabled() {
    var user = new UserEntity("user", "password");
    user.setBlocked(true);

    when(userService.getUserByUsername(any())).thenReturn(Optional.of(user));

    var userDetails = underTest.loadUserByUsername(user.getUsername());

    assertThat(userDetails.isEnabled()).isFalse();
  }

  @Test
  void adminUser_hasRoleAdminAuthority() {
    var user = new UserEntity("admin", "password");
    user.setAdmin(true);

    when(userService.getUserByUsername(any())).thenReturn(Optional.of(user));

    var userDetails = underTest.loadUserByUsername(user.getUsername());

    assertThat(userDetails.getAuthorities())
        .extracting(Object::toString)
        .containsExactly("ROLE_ADMIN");
  }

  @Test
  void regularUser_hasNoAuthorities() {
    var user = new UserEntity("user", "password");

    when(userService.getUserByUsername(any())).thenReturn(Optional.of(user));

    var userDetails = underTest.loadUserByUsername(user.getUsername());

    assertThat(userDetails.getAuthorities()).isEmpty();
  }
}
