package pl.edu.uj.notes.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
