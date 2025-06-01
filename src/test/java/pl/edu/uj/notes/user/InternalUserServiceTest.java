package pl.edu.uj.notes.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class InternalUserServiceTest {

    private static final String USERNAME = "testuser";

    @Autowired private InternalUserService internalUserService;
    @MockitoBean private UserRepository userRepository;

    @Nested
    class GetUserByUsername {

        @Test
        void whenUsernameIsValid_thenReturnUser() {
            // Given
            UserEntity expectedUser = new UserEntity(USERNAME, "password");
            when(userRepository.getUserEntityByUsername(USERNAME)).thenReturn(Optional.of(expectedUser));

            // When
            Optional<UserEntity> result = internalUserService.getUserByUsername(USERNAME);

            // Then
            assertTrue(result.isPresent());
            assertEquals(USERNAME, result.get().getUsername());
        }

        @Test
        void whenUsernameIsNull_thenThrowException() {
            // When & Then
            var exception = assertThrows(
                    IllegalArgumentException.class, () -> internalUserService.getUserByUsername(null));
            assertEquals("Username should not be null or empty", exception.getMessage());
        }

        @Test
        void whenUsernameIsEmpty_thenThrowException() {
            // When & Then
            var exception = assertThrows(
                    IllegalArgumentException.class, () -> internalUserService.getUserByUsername("   "));
            assertEquals("Username should not be null or empty", exception.getMessage());
        }

        @Test
        void whenUserNotFound_thenReturnEmptyOptional() {
            // Given
            when(userRepository.getUserEntityByUsername(USERNAME)).thenReturn(Optional.empty());

            // When
            Optional<UserEntity> result = internalUserService.getUserByUsername(USERNAME);

            // Then
            assertTrue(result.isEmpty());
        }
    }
}
