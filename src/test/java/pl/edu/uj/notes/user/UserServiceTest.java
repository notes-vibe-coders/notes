package pl.edu.uj.notes.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.edu.uj.notes.user.api.CreateUserRequest;
import pl.edu.uj.notes.user.api.ViewUsersRequest;
import pl.edu.uj.notes.user.exceptions.UserAlreadyExistsException;
import pl.edu.uj.notes.user.exceptions.UsersNotFoundException;

class UserServiceTest {

  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";
  private static final Integer ID = 1;

  private AutoCloseable closeable;

  @Mock private UserRepository userRepository;
  @InjectMocks private UserService userService;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void close() throws Exception {
    closeable.close();
  }

  @Test
  void shouldReturnUserIdWhenUserIsCreated() {
    // Given
    CreateUserRequest createUserRequest =
        CreateUserRequest.builder().username(USERNAME).password(PASSWORD).build();
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
  void shouldThrowExceptionWhenUsernameAlreadyExists() {
    // Given
    CreateUserRequest createUserRequest =
        CreateUserRequest.builder().username(USERNAME).password(PASSWORD).build();

    when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

    // When & Then
    assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(createUserRequest));
    verify(userRepository, never()).save(any(UserEntity.class));
  }

  @Test
  void shouldReturnUsernamesWhenTheyArePresentInDatabase() {
    // Given
    ViewUsersRequest viewUsersRequest = ViewUsersRequest.builder().idList(List.of(ID)).build();
    UserEntity mockUser = new UserEntity(USERNAME, PASSWORD);

    when(userRepository.findAllById(List.of(ID))).thenReturn(List.of(mockUser));

    // When
    List<String> usernames = userService.viewUsers(viewUsersRequest);

    // Then
    assertEquals(1, usernames.size());
    assertEquals(USERNAME, usernames.getFirst());
  }

  @Test
  void shouldThrowExceptionWhenUsernamesAreNotPresentInDatabase() {
    // Given
    ViewUsersRequest viewUsersRequest = ViewUsersRequest.builder().idList(List.of(ID)).build();

    when(userRepository.findAllById(List.of(ID))).thenReturn(List.of());

    // When & Then
    assertThrows(UsersNotFoundException.class, () -> userService.viewUsers(viewUsersRequest));
  }
}
