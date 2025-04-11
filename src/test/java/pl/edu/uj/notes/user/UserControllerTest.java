package pl.edu.uj.notes.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.edu.uj.notes.authentication.SecurityConfig;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

  static final String USER_URI = "/api/v1/user";

  @MockitoBean UserService userService;
  @Autowired MockMvc mockMvc;

  @Test
  void whenBlankFields_thenBadRequest() throws Exception {
    mockMvc
        .perform(post(USER_URI).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void whenCorrectRequest_thenCreated() throws Exception {
    mockMvc
        .perform(
            post(USER_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"username\",\"password\":\"password\"}"))
        .andExpect(status().isCreated());
  }

  @WithMockUser
  @DisplayName("Password update")
  @Test
  void whenCorrectPasswordUpdateRequest_thenNoContent() throws Exception {
    String json =
        """
            {
              "userId": "123e4567-e89b-12d3-a456-426614174000",
              "oldPassword": "old123",
              "newPassword": "new123"
            }
            """;

    doNothing().when(userService).updatePassword(any());

    mockMvc
        .perform(put(USER_URI + "/password").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isNoContent());
  }
}
