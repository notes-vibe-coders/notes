package pl.edu.uj.notes.user;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.uj.notes.authentication.SecurityConfig;
import pl.edu.uj.notes.authorization.AccessControlService;
import pl.edu.uj.notes.user.exception.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
public class UserServiceTest {

  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";
  private static final String ENCODED_PASSWORD = "encoded";

  @Autowired private UserRepository userRepository;
  @Autowired private UserService userService;
  @MockitoBean private PasswordEncoder passwordEncoder;
  @MockitoBean private AccessControlService accessControlService;

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
    when(accessControlService.userHasAccessTo(any(), any())).thenReturn(true);

    DeleteUserRequest deleteUserRequest = new DeleteUserRequest(userId);

    // When
    userService.deleteUser(deleteUserRequest);

    // Then
    assertFalse(userRepository.existsById(userId));
  }

  @Test
  void notAdminTryingToDeleteOtherUser_unauthorizedException() {
    UserEntity user = new UserEntity(USERNAME, PASSWORD);
    userRepository.save(user);
    String userId = user.getId();
    when(accessControlService.userHasAccessTo(any(), any())).thenReturn(false);

    DeleteUserRequest deleteUserRequest = new DeleteUserRequest(userId);

    // When
    assertThatCode(() -> userService.deleteUser(deleteUserRequest))
        .isExactlyInstanceOf(UnauthorizedUserAccessException.class);
  }

  @Test
  void whenUserDoesNotExist_thenThrowUserNotFoundException() {
    // Given
    String userId = "1e931558-2ef8-42ae-8642-3e72778de9c5";
    DeleteUserRequest deleteUserRequest = new DeleteUserRequest(userId);

    // When & Then
    UserNotFoundException exception =
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(deleteUserRequest));

    assertEquals("User not found", exception.getMessage());
    assertFalse(userRepository.existsById(userId));
  }

  @Nested
  class UpdatePassword {

    @Test
    void whenCorrectOldPassword_thenUpdatePassword() {
      // Given
      when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
      when(accessControlService.userHasAccessTo(any(), any())).thenReturn(true);

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

      when(accessControlService.userHasAccessTo(any(), any())).thenReturn(true);
      when(passwordEncoder.matches(wrongOldPassword, ENCODED_PASSWORD)).thenReturn(false);

      // When & Then
      var exception =
          assertThrows(
              InvalidOldPasswordException.class, () -> userService.updatePassword(request));
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

    @Test
    void whenUserDoesNotHaveWritePermissions_thenThrowUnauthorizedUserAccessException() {
      // Given
      when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
      when(accessControlService.userHasAccessTo(any(), any())).thenReturn(false);

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

      // When & Then
      var exception =
          assertThrows(
              UnauthorizedUserAccessException.class, () -> userService.updatePassword(request));
      assertEquals(
          "You are not allowed to update user " + request.getUserId(), exception.getMessage());
    }
  }

  @Nested
  class ViewUsers {

    @Test
    void whenUserPresentInDatabase_thenReturnUsername() {
      // Given
      UserEntity user = new UserEntity(USERNAME, PASSWORD);
      userRepository.save(user);
      String userId = user.getId();
      ViewUsersRequest viewUsersRequest =
          ViewUsersRequest.builder().idList(List.of(userId)).build();

      // When
      Map<String, String> usernamesAndIds = userService.viewUsers(viewUsersRequest);

      // Then
      assertEquals(1, usernamesAndIds.size());
      assertEquals(USERNAME, usernamesAndIds.get(userId));
    }

    @Test
    void whenUserIsNotPresentInDatabase_thenThrowException() {
      // Given
      String fakeId = "fakeId";
      ViewUsersRequest viewUsersRequest =
          ViewUsersRequest.builder().idList(List.of(fakeId)).build();

      // When & Then
      assertThrows(UsersNotFoundException.class, () -> userService.viewUsers(viewUsersRequest));
    }
  }
}
