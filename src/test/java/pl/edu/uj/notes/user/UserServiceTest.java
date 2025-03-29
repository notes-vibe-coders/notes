package pl.edu.uj.notes.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
}
