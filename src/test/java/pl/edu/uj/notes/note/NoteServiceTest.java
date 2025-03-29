package pl.edu.uj.notes.note;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

  String TITLE = "testTitle";
  String CONTENT = "testContent";

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
    void createNoteReturnsIdOfSnapshot() {
      var snapshotId = "testSnapshotId";

      var createNoteRequest = new CreateNoteRequest(TITLE, CONTENT);

      when(noteSnapshotRepository.save(any())).thenReturn(noteSnapshot);
      when(noteRepository.save(any())).thenReturn(note);
      when(noteSnapshot.getId()).thenReturn(snapshotId);

      var id = underTest.createNote(createNoteRequest);

      assertThat(id).isEqualTo(snapshotId);
    }
  }
}
