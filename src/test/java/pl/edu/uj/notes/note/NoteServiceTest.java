package pl.edu.uj.notes.note;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

      List<NoteDTO> response = underTest.getAllNotes(null, null);

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

      List<NoteDTO> response = underTest.getAllNotes(TITLE, CONTENT);

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
}
