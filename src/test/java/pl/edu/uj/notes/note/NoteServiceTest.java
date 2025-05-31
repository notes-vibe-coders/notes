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
      var createNoteRequest = new CreateNoteRequest(TITLE, CONTENT);

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
      var createNoteRequest = new CreateNoteRequest(TITLE, CONTENT);

      when(noteSnapshotRepository.save(any())).thenReturn(noteSnapshot);
      when(noteRepository.save(any())).thenReturn(note);
      when(note.getId()).thenReturn(NOTE_ID);

      var id = underTest.createNote(createNoteRequest);

      assertThat(id).isEqualTo(NOTE_ID);
    }
  }

  @Nested
  class getNotes {

    @Test
    void getNoteById_thenReturnNote() {
      when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.of(note));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(note))
          .thenReturn(Optional.of(noteSnapshot));

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
    void getNoteById_thenNoteNotFound() {
      when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.empty());

      assertThrows(NoteNotFoundException.class, () -> underTest.getNote(NOTE_ID));
    }

    @Test
    void getNoteById_thenNoteSnapshotNotFound() {
      when(noteRepository.findById(NOTE_ID)).thenReturn(Optional.of(note));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(note))
          .thenReturn(Optional.empty());

      assertThrows(NoteSnapshotNotFoundException.class, () -> underTest.getNote(NOTE_ID));
    }

    @Test
    void getAllNotes_thenReturnAllNotes() {
      when(noteRepository.findAllByTitleContainingIgnoreCase("")).thenReturn(List.of(note));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(note))
          .thenReturn(Optional.of(noteSnapshot));
      when(noteSnapshot.getContent()).thenReturn(CONTENT);

      List<NoteDTO> response = underTest.getAllNotes(null, null, null);

      verify(noteRepository, times(1)).findAllByTitleContainingIgnoreCase("");
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
    void getAllNotesContainingTitleAndContent_thenReturnAllNotes() {
      when(noteRepository.findAllByTitleContainingIgnoreCase(TITLE)).thenReturn(List.of(note));
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(note))
          .thenReturn(Optional.of(noteSnapshot));
      when(noteSnapshot.getContent()).thenReturn(CONTENT);

      List<NoteDTO> response = underTest.getAllNotes(TITLE, CONTENT, null);

      verify(noteRepository, times(1)).findAllByTitleContainingIgnoreCase(TITLE);
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
      Note importantNote = mock(Note.class);
      when(importantNote.isImportant()).thenReturn(true);
      when(importantNote.getId()).thenReturn("importantId");

      Note notImportantNote = mock(Note.class);
      when(notImportantNote.isImportant()).thenReturn(false);

      List<Note> allNotes = List.of(importantNote, notImportantNote);
      when(noteRepository.findAllByTitleContainingIgnoreCase("")).thenReturn(allNotes);
      when(noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(any()))
              .thenReturn(Optional.of(mock(NoteSnapshot.class)));

      List<NoteDTO> result = underTest.getAllNotes(null, null, true);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().important()).isTrue();
      verify(noteRepository).findAllByTitleContainingIgnoreCase("");
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
            false);
    NoteSnapshot TEST_SNAPSHOT =
        new NoteSnapshot(
            ID,
            TEST_NOTE,
            CONTENT,
            Instant.now().minus(2, MINUTES),
            Instant.now().minus(1, MINUTES));

    @Test
    void noteNotFound_throwsNotFoundException() {
      var updateRequest = new CreateNoteRequest(TITLE, CONTENT);

      assertThatCode(() -> underTest.updateNote(ID, updateRequest))
          .isExactlyInstanceOf(NoteNotFoundException.class);
    }

    @Test
    void noteNotActive_throwsNotFoundException() {
      var updateRequest = new CreateNoteRequest(TITLE, CONTENT);
      var note = TEST_NOTE.withActive(false);

      when(noteRepository.findById(any())).thenReturn(Optional.of(note));

      assertThatCode(() -> underTest.updateNote(ID, updateRequest))
          .isExactlyInstanceOf(NoteNotFoundException.class);
    }

    @Test
    void titleUpdated(@Captor ArgumentCaptor<Note> captor) {
      var newTile = TITLE + "new";
      var now = Instant.now();

      var updateRequest = new CreateNoteRequest(newTile, CONTENT);

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
      var updateRequest = new CreateNoteRequest(TITLE, CONTENT);

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
      var updateRequest = new CreateNoteRequest(TITLE, CONTENT);
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

      var updateRequest = new CreateNoteRequest(TITLE, newContent);

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
      var updateRequest = new CreateNoteRequest(TITLE, CONTENT);

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
