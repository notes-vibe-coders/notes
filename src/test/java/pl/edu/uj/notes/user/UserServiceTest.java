package pl.edu.uj.notes.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.uj.notes.authentication.SecurityConfig;
import pl.edu.uj.notes.user.exceptions.UserAlreadyExistsException;
import pl.edu.uj.notes.user.exceptions.UserNotFoundException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(SecurityConfig.class)
public class UserServiceTest {

  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";
  private static final String ENCODED_PASSWORD = "encoded";

  @Autowired private UserRepository userRepository;
  @Autowired private UserService userService;
  @MockitoBean private PasswordEncoder passwordEncoder;

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
  }

  @Nested
  class CreateUser {

    @Test
    void whenCreateUser_thenReturnsUserId() {
      // Given
      CreateUserRequest createUserRequest = new CreateUserRequest(USERNAME, PASSWORD);

      // When
      String id = userService.createUser(createUserRequest);

      // Then
      assertTrue(userRepository.existsById(id));
      assertTrue(userRepository.existsByUsername(createUserRequest.getUsername()));
    }

    @Test
    void whenUsernameAlreadyExists_thenThrowException() {
      // Given
      CreateUserRequest createUserRequest = new CreateUserRequest(USERNAME, PASSWORD);

      // When & Then
      assertDoesNotThrow(() -> userService.createUser(createUserRequest));
      assertThrows(
          UserAlreadyExistsException.class, () -> userService.createUser(createUserRequest));
    }

    @Test
    void createsUserWithEncodedPassword() {
      var request = new CreateUserRequest(USERNAME, PASSWORD);

      when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);

      userService.createUser(request);
      UserEntity user = userRepository.getUserEntityByUsername(USERNAME).orElseThrow();

      assertEquals(ENCODED_PASSWORD, user.getPassword());
    }
  }

  @Test
  void whenUserExists_thenDeleteUserSuccessfully() {
    // Given
    UserEntity user = new UserEntity(USERNAME, PASSWORD);
    userRepository.save(user);
    String userId = user.getId();

    DeleteUserRequest deleteUserRequest = new DeleteUserRequest(userId);

    // When
    userService.deleteUser(deleteUserRequest);

    // Then
    assertFalse(userRepository.existsById(userId));
  }

  @Test
  void whenUserDoesNotExist_thenThrowUserNotFoundException() {
    // Given
    String userId = "1e931558-2ef8-42ae-8642-3e72778de9c5";
    DeleteUserRequest deleteUserRequest = new DeleteUserRequest(userId);

    // When & Then
    UserNotFoundException exception =
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(deleteUserRequest));

    assertEquals("User with ID " + userId + " does not exist", exception.getMessage());
    assertFalse(userRepository.existsById(userId));
  }

  @Nested
  class UpdatePassword {

    @Test
    void whenCorrectOldPassword_thenUpdatePassword() {
      // Given
      when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);

      UserEntity user = new UserEntity(USERNAME, ENCODED_PASSWORD);
      userRepository.save(user);
      String userId = user.getId();

      String newPassword = "newSecret123";
      String encodedNewPassword = "encodedNewPassword";
      when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
      when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

      UpdatePasswordRequest request = new UpdatePasswordRequest();
      request.setUserId(userId);
      request.setOldPassword(PASSWORD);
      request.setNewPassword(newPassword);

      // When
      userService.updatePassword(request);

      // Then
      UserEntity updated = userRepository.findById(userId).orElseThrow();
      assertEquals(encodedNewPassword, updated.getPassword());
    }

    @Test
    void whenIncorrectOldPassword_thenThrowException() {
      // Given
      String wrongOldPassword = "wrong-password";
      UserEntity user = new UserEntity(USERNAME, ENCODED_PASSWORD);
      userRepository.save(user);
      String userId = user.getId();

      UpdatePasswordRequest request = new UpdatePasswordRequest();
      request.setUserId(userId);
      request.setOldPassword(wrongOldPassword);
      request.setNewPassword("irrelevant");

      when(passwordEncoder.matches(wrongOldPassword, ENCODED_PASSWORD)).thenReturn(false);

      // When & Then
      var exception =
          assertThrows(IllegalArgumentException.class, () -> userService.updatePassword(request));
      assertEquals("Old password is incorrect", exception.getMessage());
    }

    @Test
    void whenUserNotFound_thenThrowUserNotFoundException() {
      // Given
      String userId = UUID.randomUUID().toString();
      UpdatePasswordRequest request = new UpdatePasswordRequest();
      request.setUserId(userId);
      request.setOldPassword("any");
      request.setNewPassword("any");

      // When & Then
      var exception =
          assertThrows(UserNotFoundException.class, () -> userService.updatePassword(request));
      assertEquals("User not found", exception.getMessage());
    }
  }
}
