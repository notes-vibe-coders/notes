package pl.edu.uj.notes.note;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.edu.uj.notes.authentication.SecurityConfig;

@WebMvcTest(SnapshotController.class)
@Import(SecurityConfig.class)
class SnapshotControllerTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean SnapshotService snapshotService;

  private static final String NOTE_ID = "note-123";
  private static final String SNAPSHOT_ID = "snap-456";

  @WithMockUser
  @Test
  void whenGetSnapshots_thenReturnsOk() throws Exception {
    // Given
    SnapshotDTO snapshot = new SnapshotDTO("snap-1", NOTE_ID, "text content", 1717242000000L);
    when(snapshotService.getSnapshotsByNoteId(NOTE_ID)).thenReturn(List.of(snapshot));

    // When & Then
    mockMvc.perform(get("/api/v1/notes/{noteId}/snapshot", NOTE_ID)).andExpect(status().isOk());
  }

  @WithMockUser
  @Test
  void whenRestoreSnapshot_thenReturnsOk() throws Exception {
    // Given
    SnapshotDTO restored =
        new SnapshotDTO(SNAPSHOT_ID, NOTE_ID, "restored content", 1717242000000L);
    when(snapshotService.restoreSnapshot(NOTE_ID, SNAPSHOT_ID)).thenReturn(restored);

    // When & Then
    mockMvc
        .perform(
            patch("/api/v1/notes/{noteId}/snapshot/{snapshotId}", NOTE_ID, SNAPSHOT_ID)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }
}
