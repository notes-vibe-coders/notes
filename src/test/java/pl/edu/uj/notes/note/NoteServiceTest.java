package pl.edu.uj.notes.note;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.uj.notes.note.exception.NoteAccessDeniedException;
import pl.edu.uj.notes.note.exception.NoteNotFoundException;
import pl.edu.uj.notes.note.exception.NoteSnapshotNotFoundException;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

  String TITLE = "testTitle";
  String CONTENT = "testContent";
  String NOTE_ID = "noteId";
  String ID = "testId";

  @Mock NoteSnapshotRepository noteSnapshotRepository;
  @Mock NoteRepository noteRepository;
  @InjectMocks NoteService underTest;

  @Mock Note note;
  @Mock NoteSnapshot noteSnapshot;

  @Nested
  class createNote {

    @Captor ArgumentCaptor<Note> noteArgumentCaptor;
    @Captor ArgumentCaptor<NoteSnapshot> noteSnapshotArgumentCaptor;

    @Test
    void createNoteShouldCreateNoteAndSnapshot() {
      var createNoteRequest = new CreateNoteRequest(TITLE, CONTENT, null);

      when(noteSnapshotRepository.save(any())).thenReturn(noteSnapshot);
      when(noteRepository.save(any())).thenReturn(note);

      underTest.createNote(createNoteRequest);

      verify(noteRepository).save(noteArgumentCaptor.capture());
      verify(noteSnapshotRepository).save(noteSnapshotArgumentCaptor.capture());

      assertThat(noteArgumentCaptor.getValue().getTitle()).isEqualTo(TITLE);
      assertThat(noteSnapshotArgumentCaptor.getValue())
          .extracting(NoteSnapshot::getNoteId, NoteSnapshot::getContent)
          .containsExactly(note, CONTENT);
    }

    @Test
    void createNoteReturnsId() {
      var createNoteRequest = new CreateNoteRequest(TITLE, CONTENT, null);

      when(noteSnapshotRepository.save(any())).thenReturn(noteSnapshot);
      when(noteRepository.save(any())).thenReturn(note);
      when(note.getId()).thenReturn(NOTE_ID);

      var id = underTest.createNote(createNoteRequest);

      assertThat(id).isEqualTo(NOTE_ID);
    }

    @Test
    void createNote_withPassword_shouldSetPasswordHash() {
      String rawPassword = "superSecret";
      ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);

      when(noteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
      when(noteSnapshotRepository.save(any())).thenReturn(noteSnapshot);

      CreateNoteRequest request = new CreateNoteRequest("title", "content", rawPassword);
      underTest.createNote(request);

      verify(noteRepository).save(captor.capture());

      Note savedNote = captor.getValue();
      assertThat(savedNote.getPasswordHash()).isNotNull();
      assertThat(savedNote.isPasswordCorrect(rawPassword)).isTrue();
    }
  }

  @Nested
  class getNotes {

    @Test
    void getNoteById_thenReturnNote() {
      when(noteRepository.findByActiveAndId(true, NOTE_ID)).thenReturn(Optional.of(note));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(note))
          .thenReturn(Optional.of(noteSnapshot));

      when(note.getPasswordHash()).thenReturn(null);

      NoteDTO response = underTest.getNote(NOTE_ID);

      assertThat(response)
          .extracting(
              NoteDTO::id, NoteDTO::title, NoteDTO::content, NoteDTO::createdAt, NoteDTO::updatedAt)
          .containsExactly(
              note.getId(),
              note.getTitle(),
              noteSnapshot.getContent(),
              note.getCreatedAt(),
              note.getUpdatedAt());
    }

    @Test
    void getNoteById_whenPasswordProtected_thenThrowSecurityException() {
      when(noteRepository.findByActiveAndId(true, NOTE_ID)).thenReturn(Optional.of(note));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(note))
          .thenReturn(Optional.of(noteSnapshot));
      when(note.getPasswordHash()).thenReturn("someHash");

      assertThrows(NoteAccessDeniedException.class, () -> underTest.getNote(NOTE_ID));
    }

    @Test
    void getNoteById_thenNoteNotFound() {
      when(noteRepository.findByActiveAndId(true, NOTE_ID)).thenReturn(Optional.empty());

      assertThrows(NoteNotFoundException.class, () -> underTest.getNote(NOTE_ID));
    }

    @Test
    void getNoteById_thenNoteSnapshotNotFound() {
      when(noteRepository.findByActiveAndId(true, NOTE_ID)).thenReturn(Optional.of(note));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(note))
          .thenReturn(Optional.empty());

      assertThrows(NoteSnapshotNotFoundException.class, () -> underTest.getNote(NOTE_ID));
    }

    @Test
    void getNoteWithPassword_whenPasswordCorrect_returnsNote() {
      Instant now = Instant.now();

      when(noteRepository.findByActiveAndId(true, NOTE_ID)).thenReturn(Optional.of(note));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(note))
          .thenReturn(Optional.of(noteSnapshot));

      when(note.isPasswordCorrect("pass123")).thenReturn(true);

      when(note.getId()).thenReturn(NOTE_ID);
      when(note.getTitle()).thenReturn(TITLE);
      when(note.getCreatedAt()).thenReturn(now);
      when(note.getUpdatedAt()).thenReturn(now);
      when(note.isImportant()).thenReturn(false);

      when(noteSnapshot.getContent()).thenReturn(CONTENT);
      when(noteSnapshot.getUpdatedAt()).thenReturn(now);

      NoteDTO dto = underTest.getNoteWithPassword(NOTE_ID, "pass123");

      assertThat(dto.id()).isEqualTo(NOTE_ID);
      assertThat(dto.title()).isEqualTo(TITLE);
      assertThat(dto.content()).isEqualTo(CONTENT);
      assertThat(dto.createdAt()).isEqualTo(now);
      assertThat(dto.updatedAt()).isEqualTo(now);
      assertThat(dto.important()).isFalse();
    }

    @Test
    void getNoteWithPassword_whenPasswordIncorrect_throwsSecurityException() {
      when(noteRepository.findByActiveAndId(true, NOTE_ID)).thenReturn(Optional.of(note));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(note))
          .thenReturn(Optional.of(noteSnapshot));
      when(note.isPasswordCorrect("wrong")).thenReturn(false);

      assertThrows(
          NoteAccessDeniedException.class, () -> underTest.getNoteWithPassword(NOTE_ID, "wrong"));
    }

    @Test
    void getAllNotes_thenReturnAllNotes() {
      when(noteRepository.findAllByTitleContainingIgnoreCaseAndActive("", true))
          .thenReturn(List.of(note));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(note))
          .thenReturn(Optional.of(noteSnapshot));
      when(noteSnapshot.getContent()).thenReturn(CONTENT);

      when(note.getPasswordHash()).thenReturn(null);

      List<NoteDTO> response = underTest.getAllNotes(null, null, null);

      verify(noteRepository, times(1)).findAllByTitleContainingIgnoreCaseAndActive("", true);
      verify(noteSnapshotRepository, times(1)).findFirstByNoteIdOrderByCreatedAtDesc(note);

      assertEquals(1, response.size());
      assertThat(response.getFirst())
          .extracting(
              NoteDTO::id, NoteDTO::title, NoteDTO::content, NoteDTO::createdAt, NoteDTO::updatedAt)
          .containsExactly(
              note.getId(),
              note.getTitle(),
              noteSnapshot.getContent(),
              note.getCreatedAt(),
              note.getUpdatedAt());
    }

    @Test
    void getAllNotes_whenNoteIsPasswordProtected_thenSkipped() {
      when(noteRepository.findAllByTitleContainingIgnoreCaseAndActive("", true))
          .thenReturn(List.of(note));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(note))
          .thenReturn(Optional.of(noteSnapshot));
      when(note.getPasswordHash()).thenReturn("hash");

      List<NoteDTO> result = underTest.getAllNotes(null, null, null);

      assertThat(result).isEmpty();
    }

    @Test
    void getAllNotesContainingTitleAndContent_thenReturnAllNotes() {
      when(noteRepository.findAllByTitleContainingIgnoreCaseAndActive(TITLE, true))
          .thenReturn(List.of(note));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(note))
          .thenReturn(Optional.of(noteSnapshot));
      when(noteSnapshot.getContent()).thenReturn(CONTENT);

      List<NoteDTO> response = underTest.getAllNotes(TITLE, CONTENT, null);

      verify(noteRepository, times(1)).findAllByTitleContainingIgnoreCaseAndActive(TITLE, true);
      verify(noteSnapshotRepository, times(1)).findFirstByNoteIdOrderByCreatedAtDesc(note);

      assertEquals(1, response.size());
      assertThat(response.getFirst())
          .extracting(
              NoteDTO::id, NoteDTO::title, NoteDTO::content, NoteDTO::createdAt, NoteDTO::updatedAt)
          .containsExactly(
              note.getId(),
              note.getTitle(),
              noteSnapshot.getContent(),
              note.getCreatedAt(),
              note.getUpdatedAt());
    }

    @Test
    void getAllImportantNotes_thenReturnAllNotes() {
      Note importantNote =
          new Note("id1", "Important", Instant.now(), Instant.now(), true, true, null);
      Note notImportantNote =
          new Note("id2", "Not Important", Instant.now(), Instant.now(), true, false, null);

      List<Note> allNotes = List.of(importantNote, notImportantNote);
      when(noteRepository.findAllByTitleContainingIgnoreCaseAndActive("", true))
          .thenReturn(allNotes);

      NoteSnapshot snapshot = mock(NoteSnapshot.class);
      when(snapshot.getContent()).thenReturn("Snapshot content");
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(any(Note.class)))
          .thenReturn(Optional.of(snapshot));

      List<NoteDTO> result = underTest.getAllNotes(null, null, true);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().important()).isTrue();
      verify(noteRepository).findAllByTitleContainingIgnoreCaseAndActive("", true);
    }
  }

  @Nested
  class deleteNote {

    @Test
    void deleteNoteDeactivatesExistingNote() {
      var deleteNoteRequest = new DeleteNoteRequest(ID);

      when(noteRepository.findById(ID)).thenReturn(Optional.of(note));

      underTest.deleteNote(deleteNoteRequest);

      verify(note).setActive(false);
      verify(noteRepository).save(note);

      assertThat(note.isActive()).isFalse();
    }

    @Test
    void deleteNoteThrowsExceptionWhenNoteDoesNotExist() {
      var deleteNoteRequest = new DeleteNoteRequest(ID);

      when(noteRepository.findById(ID)).thenReturn(Optional.empty());

      NoteNotFoundException e =
          assertThrows(NoteNotFoundException.class, () -> underTest.deleteNote(deleteNoteRequest));

      assertThat(e.getMessage()).isEqualTo("Note with ID " + ID + " does not exist");
    }
  }

  @Nested
  class updateNote {

    Note TEST_NOTE =
        new Note(
            ID,
            TITLE,
            Instant.now().minus(10, MINUTES),
            Instant.now().minus(5, MINUTES),
            true,
            false,
            null);
    NoteSnapshot TEST_SNAPSHOT =
        new NoteSnapshot(
            ID,
            TEST_NOTE,
            CONTENT,
            Instant.now().minus(2, MINUTES),
            Instant.now().minus(1, MINUTES));

    @Test
    void noteNotFound_throwsNotFoundException() {
      var updateRequest = new CreateNoteRequest(TITLE, CONTENT, null);

      assertThatCode(() -> underTest.updateNote(ID, updateRequest))
          .isExactlyInstanceOf(NoteNotFoundException.class);
    }

    @Test
    void noteNotActive_throwsNotFoundException() {
      var updateRequest = new CreateNoteRequest(TITLE, CONTENT, null);
      var note = TEST_NOTE.withActive(false);

      when(noteRepository.findById(any())).thenReturn(Optional.of(note));

      assertThatCode(() -> underTest.updateNote(ID, updateRequest))
          .isExactlyInstanceOf(NoteNotFoundException.class);
    }

    @Test
    void titleUpdated(@Captor ArgumentCaptor<Note> captor) {
      var newTile = TITLE + "new";
      var now = Instant.now();

      var updateRequest = new CreateNoteRequest(newTile, CONTENT, null);

      when(noteRepository.findById(any())).thenReturn(Optional.of(TEST_NOTE));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(any()))
          .thenReturn(Optional.of(TEST_SNAPSHOT));
      when(noteRepository.save(any())).thenReturn(TEST_NOTE.withTitle(newTile).withUpdatedAt(now));

      var result = underTest.updateNote(ID, updateRequest);

      assertThat(result)
          .isEqualTo(
              new NoteDTO(
                  ID, newTile, CONTENT, TEST_NOTE.getCreatedAt(), now, TEST_NOTE.isImportant()));

      verify(noteRepository).save(captor.capture());

      assertThat(captor.getValue()).extracting(Note::getTitle).isEqualTo(newTile);
    }

    @Test
    void noChangesInTitle() {
      var updateRequest = new CreateNoteRequest(TITLE, CONTENT, null);

      when(noteRepository.findById(any())).thenReturn(Optional.of(TEST_NOTE));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(any()))
          .thenReturn(Optional.of(TEST_SNAPSHOT));

      var result = underTest.updateNote(ID, updateRequest);

      assertThat(result)
          .isEqualTo(
              new NoteDTO(
                  ID,
                  TITLE,
                  CONTENT,
                  TEST_NOTE.getCreatedAt(),
                  TEST_SNAPSHOT.getUpdatedAt(),
                  TEST_NOTE.isImportant()));

      verify(noteRepository, never()).save(any());
    }

    @Test
    void noLatestSnapshot() {
      var updateRequest = new CreateNoteRequest(TITLE, CONTENT, null);
      var now = Instant.now();

      when(noteRepository.findById(any())).thenReturn(Optional.of(TEST_NOTE));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(any()))
          .thenReturn(Optional.empty());
      when(noteSnapshotRepository.save(any())).thenReturn(TEST_SNAPSHOT.withUpdatedAt(now));

      var result = underTest.updateNote(ID, updateRequest);

      assertThat(result)
          .isEqualTo(
              new NoteDTO(
                  ID, TITLE, CONTENT, TEST_NOTE.getCreatedAt(), now, TEST_NOTE.isImportant()));

      verify(noteSnapshotRepository).save(new NoteSnapshot(TEST_NOTE, CONTENT));
    }

    @Test
    void contentUpdated() {
      var newContent = CONTENT + "new";
      var now = Instant.now();

      var updateRequest = new CreateNoteRequest(TITLE, newContent, null);

      when(noteRepository.findById(any())).thenReturn(Optional.of(TEST_NOTE));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(any()))
          .thenReturn(Optional.of(TEST_SNAPSHOT));
      when(noteSnapshotRepository.save(any())).thenReturn(TEST_SNAPSHOT.withUpdatedAt(now));

      var result = underTest.updateNote(ID, updateRequest);

      assertThat(result)
          .isEqualTo(
              new NoteDTO(
                  ID, TITLE, CONTENT, TEST_NOTE.getCreatedAt(), now, TEST_NOTE.isImportant()));

      verify(noteSnapshotRepository).save(new NoteSnapshot(TEST_NOTE, newContent));
    }

    @Test
    void noChangesInContent() {
      var updateRequest = new CreateNoteRequest(TITLE, CONTENT, null);

      when(noteRepository.findById(any())).thenReturn(Optional.of(TEST_NOTE));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(any()))
          .thenReturn(Optional.of(TEST_SNAPSHOT));

      var result = underTest.updateNote(ID, updateRequest);

      assertThat(result)
          .isEqualTo(
              new NoteDTO(
                  ID,
                  TITLE,
                  CONTENT,
                  TEST_NOTE.getCreatedAt(),
                  TEST_SNAPSHOT.getUpdatedAt(),
                  TEST_NOTE.isImportant()));

      verify(noteSnapshotRepository, never()).save(any());
    }

    @Test
    void updateNote_withPassword_shouldUpdateHash() {
      String newPassword = "newPassword123";
      Instant now = Instant.now();

      Note note =
          new Note(
              NOTE_ID, TITLE, now.minus(10, MINUTES), now.minus(5, MINUTES), true, false, null);

      NoteSnapshot previousSnapshot = new NoteSnapshot(note, CONTENT);
      previousSnapshot.setCreatedAt(now.minus(2, MINUTES));
      previousSnapshot.setUpdatedAt(now.minus(1, MINUTES));

      NoteSnapshot savedSnapshot = new NoteSnapshot(note, CONTENT);
      savedSnapshot.setCreatedAt(now);
      savedSnapshot.setUpdatedAt(now);

      CreateNoteRequest request = new CreateNoteRequest(TITLE, CONTENT, newPassword);

      when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.of(note));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(note))
          .thenReturn(Optional.of(previousSnapshot));

      when(noteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

      NoteDTO result = underTest.updateNote(NOTE_ID, request);

      assertThat(note.getPasswordHash()).isNotNull();
      assertThat(note.isPasswordCorrect(newPassword)).isTrue();

      assertThat(result.id()).isEqualTo(NOTE_ID);
      assertThat(result.title()).isEqualTo(TITLE);
      assertThat(result.content()).isEqualTo(CONTENT);
      assertThat(result.important()).isEqualTo(false);

      verify(noteRepository, atLeastOnce()).save(note);
    }
  }

  @Nested
  class markAsImportant {

    @Test
    void noteDoesNotExist_throwsException() {
      when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.empty());

      assertThrows(NoteNotFoundException.class, () -> underTest.markAsImportant(NOTE_ID));
    }

    @Test
    void noteExists_marksAsImportantAndSaves() {
      when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.of(note));
      when(noteRepository.save(any())).thenReturn(note);

      underTest.markAsImportant(NOTE_ID);

      verify(note).setImportant(true);
      verify(noteRepository).save(note);
    }
  }
}
