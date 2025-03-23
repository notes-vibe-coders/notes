package pl.edu.uj.notes.health;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.edu.uj.notes.authentication.SecurityConfig;
import pl.edu.uj.notes.user.UserService;

@WebMvcTest(controllers = HealthController.class)
@Import(SecurityConfig.class)
class HealthControllerTest {

  @Autowired MockMvc mockMvc;
  @MockitoBean UserService userService;

  @Test
  void notAuthenticatedUser_200() throws Exception {
    mockMvc.perform(get("/health")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void authenticatedUser_200() throws Exception {
    mockMvc.perform(get("/health")).andExpect(status().isOk());
  }
}
