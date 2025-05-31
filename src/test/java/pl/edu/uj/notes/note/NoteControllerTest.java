package pl.edu.uj.notes.note;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
  void deleteNote_blankId_badRequest() throws Exception {
    var request =
        """
            {
              "id": null
            }
            """;

    mockMvc
        .perform(delete(NOTE_URI).contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void deleteNote_noteNotFound_returnsNotFound() throws Exception {
    String id = "non-existing-id";
    DeleteNoteRequest deleteNoteRequest = new DeleteNoteRequest(id);
    var request =
        """
            {
              "id": "non-existing-id"
            }
            """;

    Mockito.doThrow(new NoteNotFoundException("Note with ID " + id + " does not exist"))
        .when(noteService)
        .deleteNote(deleteNoteRequest);

    mockMvc
        .perform(delete(NOTE_URI).contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  void deleteNote_noteExists_noContent() throws Exception {
    String id = "existing-id";

    var request =
        """
            {
              "id": "existing-id"
            }
            """;

    Mockito.doNothing().when(noteService).deleteNote(new DeleteNoteRequest(id));

    mockMvc
        .perform(delete(NOTE_URI).contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isNoContent());
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

  @Nested
  class createNote {

    @Test
    @WithMockUser
    void callsToUpdateNote() throws Exception {
      var request =
          """
            {
              "title": "testTitle",
              "content": "testContent"
            }
            """;

      mockMvc
          .perform(
              put(NOTE_URI + "/noteId").content(request).contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isAccepted());

      verify(noteService).updateNote(any(), any());
    }
  }

  @Nested
  class markAsImportant {

    @Test
    @WithMockUser
    void success_statusNoContent() throws Exception {
      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                  NOTE_URI + "/noteId/important"))
          .andExpect(status().isNoContent());

      verify(noteService).markAsImportant("noteId");
    }

    @Test
    @WithMockUser
    void noteNotFound_returnsNotFound() throws Exception {
      String id = "nonExistingId";

      Mockito.doThrow(new NoteNotFoundException("Note with ID " + id + " does not exist"))
          .when(noteService)
          .markAsImportant(id);

      mockMvc
          .perform(
              patch(NOTE_URI + "/" + id + "/important").contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }
  }
}
