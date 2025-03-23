package pl.edu.uj.notes.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
  @WithMockUser
  void whenBlankFields_thenBadRequest() throws Exception {
    mockMvc
        .perform(post(USER_URI).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void whenCorrectRequest_thenCreated() throws Exception {
    mockMvc
        .perform(
            post(USER_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"username\",\"password\":\"password\"}"))
        .andExpect(status().isCreated());
  }

  @Test
  void whenValidUpdateRequest_thenNoContent() throws Exception {
    int userId = 1;
    String payload = "{\"username\":\"newUser\",\"password\":\"newPassword\"}";

    mockMvc
            .perform(
                    put(USER_URI + "/" + userId).contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isNoContent());
  }
  void notAuthenticated_401() throws Exception {
    mockMvc
        .perform(post(USER_URI).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnauthorized());
  }
}
