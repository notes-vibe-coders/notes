package pl.edu.uj.notes.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.edu.uj.notes.user.exceptions.UserAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";
  private static final String ENCODED_PASSWORD = "encoded";

  @Mock private UserRepository userRepository;
  @Mock PasswordEncoder passwordEncoder;
  @InjectMocks private UserService userService;

  @Nested
  class createUser {

    @Test
    void whenCreateUser_thenReturnsUserId() {
      // Given
      CreateUserRequest createUserRequest = new CreateUserRequest(USERNAME, PASSWORD);
      UserEntity mockUser = new UserEntity(USERNAME, PASSWORD);

      when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
      when(userRepository.save(any(UserEntity.class))).thenReturn(mockUser);

      // When
      int id = userService.createUser(createUserRequest);

      // Then
      assertEquals(0, id);
      verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void whenUsernameAlreadyExists_thenThrowException() {
      // Given
      CreateUserRequest createUserRequest = new CreateUserRequest(USERNAME, PASSWORD);

      when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

      // When & Then
      assertThrows(
          UserAlreadyExistsException.class, () -> userService.createUser(createUserRequest));
      verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void createsUserWithEncodedPassword(@Captor ArgumentCaptor<UserEntity> captor) {
      var request = new CreateUserRequest(USERNAME, PASSWORD);

      when(userRepository.existsByUsername(any())).thenReturn(false);
      when(passwordEncoder.encode(any())).thenReturn(ENCODED_PASSWORD);

      userService.createUser(request);

      verify(userRepository).save(captor.capture());

      assertThat(captor.getValue())
          .extracting(UserEntity::getUsername, UserEntity::getPassword)
          .containsExactly(USERNAME, ENCODED_PASSWORD);
    }
  }
}
