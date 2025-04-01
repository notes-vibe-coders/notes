package pl.edu.uj.notes.note;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import pl.edu.uj.notes.note.exception.NoteNotFoundException;
import pl.edu.uj.notes.user.UserService;

@WebMvcTest(NoteController.class)
@Import(SecurityConfig.class)
class NoteControllerTest {

  static final String NOTE_URI = "/api/v1/notes";

  @MockitoBean NoteService noteService;
  @MockitoBean UserService userService;
  @Autowired MockMvc mockMvc;

  @Test
  @WithMockUser
  void blankTitle_badRequest() throws Exception {
    var request =
        """
            {
              "title": null,
              "content": "content"
            }
            """;

    mockMvc
        .perform(post(NOTE_URI).contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void blankContent_badRequest() throws Exception {
    var request =
        """
            {
              "title": "testTitle",
              "content": null
            }
            """;

    mockMvc
        .perform(post(NOTE_URI).contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void happyPath_callsToCreateNote() throws Exception {
    var request =
        """
            {
              "title": "testTitle",
              "content": "testContent"
            }
            """;

    mockMvc
        .perform(post(NOTE_URI).contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser
  void happyPath_callsToGetNoteById() throws Exception {
    mockMvc
        .perform(get(NOTE_URI + "/noteId").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void happyPath_callsToGetAllNotes() throws Exception {
    mockMvc
        .perform(get(NOTE_URI).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void noteNotFound_statusNotFound() throws Exception {
    when(noteService.getNote(anyString())).thenThrow(NoteNotFoundException.class);

    mockMvc
        .perform(get(NOTE_URI + "/noteId").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
