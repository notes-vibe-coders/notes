package pl.edu.uj.notes.note;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.uj.notes.note.exceptions.NoteNotFoundException;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

  String TITLE = "testTitle";
  String CONTENT = "testContent";
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
      var noteId = "noteId";

      var createNoteRequest = new CreateNoteRequest(TITLE, CONTENT);

      when(noteSnapshotRepository.save(any())).thenReturn(noteSnapshot);
      when(noteRepository.save(any())).thenReturn(note);
      when(note.getId()).thenReturn(noteId);

      var id = underTest.createNote(createNoteRequest);

      assertThat(id).isEqualTo(noteId);
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
          assertThrows(
              NoteNotFoundException.class,
              () -> {
                underTest.deleteNote(deleteNoteRequest);
              });

      assertThat(e.getMessage()).isEqualTo("Note with ID " + ID + " does not exist");
    }
  }
}
