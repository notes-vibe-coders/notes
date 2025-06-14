package pl.edu.uj.notes.note;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.uj.notes.authentication.SecurityConfig;
import pl.edu.uj.notes.note.exception.NoteNotFoundException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(SecurityConfig.class)
public class SnapshotServiceTest {

  @Autowired private SnapshotService snapshotService;

  @MockitoBean private NoteRepository noteRepository;
  @MockitoBean private NoteSnapshotRepository snapshotRepository;

  private static final String NOTE_ID = "note-123";
  private static final String SNAPSHOT_ID = "snap-456";

  @Nested
  class GetSnapshotsByNoteId {

    @Test
    void whenNoteExists_thenReturnSnapshots() {
      // Given
      Note note = new Note();
      note.setId(NOTE_ID);
      NoteSnapshot snapshot = new NoteSnapshot();
      snapshot.setId(SNAPSHOT_ID);
      snapshot.setNoteId(note);
      snapshot.setContent("text");
      snapshot.setCreatedAt(Instant.now());

      when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.of(note));
      when(snapshotRepository.findAllByNoteId(note)).thenReturn(List.of(snapshot));

      // When
      List<SnapshotDTO> result = snapshotService.getSnapshotsByNoteId(NOTE_ID);

      // Then
      assertEquals(1, result.size());
      assertEquals(SNAPSHOT_ID, result.getFirst().id());
    }

    @Test
    void whenNoteDoesNotExist_thenThrowNoteNotFoundException() {
      // Given
      when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.empty());

      // When & Then
      NoteNotFoundException ex =
          assertThrows(
              NoteNotFoundException.class, () -> snapshotService.getSnapshotsByNoteId(NOTE_ID));

      assertEquals("Note not found", ex.getMessage());
    }
  }

  @Nested
  class RestoreSnapshot {

    @Test
    void whenNoteAndSnapshotExist_thenRestoreSuccessfully() {
      // Given
      Note note = new Note();
      note.setId(NOTE_ID);
      NoteSnapshot snapshot = new NoteSnapshot();
      snapshot.setId(SNAPSHOT_ID);
      snapshot.setNoteId(note);
      snapshot.setContent("restored content");
      snapshot.setCreatedAt(Instant.now());

      when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.of(note));
      when(snapshotRepository.findById(SNAPSHOT_ID)).thenReturn(Optional.of(snapshot));
      when(snapshotRepository.save(snapshot)).thenReturn(snapshot);

      // When
      SnapshotDTO result = snapshotService.restoreSnapshot(NOTE_ID, SNAPSHOT_ID);

      // Then
      assertEquals(SNAPSHOT_ID, result.id());
      assertEquals("restored content", result.content());
    }

    @Test
    void whenNoteDoesNotExist_thenThrowNoteNotFoundException() {
      // Given
      when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.empty());

      // When & Then
      NoteNotFoundException ex =
          assertThrows(
              NoteNotFoundException.class,
              () -> snapshotService.restoreSnapshot(NOTE_ID, SNAPSHOT_ID));

      assertEquals("Note not found", ex.getMessage());
    }
  }
}
