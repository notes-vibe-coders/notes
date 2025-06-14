package pl.edu.uj.notes.note;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.uj.notes.note.exception.NoteNotFoundException;
import pl.edu.uj.notes.note.exception.UnauthorizedNoteAccessException;
import pl.edu.uj.notes.user.CreateUserRequest;
import pl.edu.uj.notes.user.UserEntity;
import pl.edu.uj.notes.user.UserService;

@SpringBootTest
@Transactional
class NoteServiceTest {

  @Autowired NoteService underTest;
  @Autowired NoteRepository noteRepository;
  @Autowired NoteSnapshotRepository noteSnapshotRepository;
  @Autowired UserService userService;

  UserEntity owner;
  UserEntity otherUser;
  Note ownerNote;
  Note otherUserNote;
  NoteSnapshot ownerNoteSnapshot;
  NoteSnapshot otherUserNoteSnapshot;

  @BeforeEach
  void setUp() {
    noteSnapshotRepository.deleteAll();
    noteRepository.deleteAll();

    owner = createAndGetUser("owner", "password");
    otherUser = createAndGetUser("other", "password");

    ownerNote = noteRepository.save(new Note("First Note", owner));
    otherUserNote = noteRepository.save(new Note("Second Note", otherUser).withImportant(true));

    ownerNoteSnapshot = noteSnapshotRepository.save(new NoteSnapshot(ownerNote, "First content"));
    otherUserNoteSnapshot =
        noteSnapshotRepository.save(new NoteSnapshot(otherUserNote, "Second content"));

    setCurrentUser(owner);
  }

  UserEntity createAndGetUser(String username, String password) {
    String userId = userService.createUser(new CreateUserRequest(username, password));
    return new UserEntity(username, password).withId(userId);
  }

  void setCurrentUser(UserEntity user) {
    Authentication auth =
        new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
  }

  @Nested
  class CreateNote {

    @Test
    void shouldThrowWhenCreatingNoteWithNullRequest() {
      setCurrentUser(owner);
      assertThatThrownBy(() -> underTest.createNote(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldCreateNewNoteWithSnapshotWhenValidRequest() {
      setCurrentUser(owner);
      var request = new CreateNoteRequest("New Note", "New Content");

      String noteId = underTest.createNote(request);

      var savedNote = noteRepository.findById(noteId).orElseThrow();
      var savedSnapshot =
          noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(savedNote).orElseThrow();

      assertThat(savedNote.getTitle()).isEqualTo("New Note");
      assertThat(savedNote.getOwner())
          .extracting(UserEntity::getId, UserEntity::getUsername)
          .containsExactly(owner.getId(), owner.getUsername());
      assertThat(savedSnapshot.getContent()).isEqualTo("New Content");
      assertThat(savedSnapshot.getNoteId()).isEqualTo(savedNote);
    }
  }

  @Nested
  class UpdateNote {

    @Test
    void shouldThrowWhenUpdatingWithNullRequest() {
      setCurrentUser(owner);
      assertThatThrownBy(() -> underTest.updateNote(ownerNote.getId(), null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenUpdatingWithNullId() {
      setCurrentUser(owner);
      var request = new CreateNoteRequest("Title", "Content");
      assertThatThrownBy(() -> underTest.updateNote(null, request))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldUpdateNoteTitleWhenTitleChanged() {
      setCurrentUser(owner);
      var request = new CreateNoteRequest("Updated Title", ownerNoteSnapshot.getContent());

      var result = underTest.updateNote(ownerNote.getId(), request);

      assertThat(result.title()).isEqualTo("Updated Title");
      assertThat(result.content()).isEqualTo(ownerNoteSnapshot.getContent());

      var updatedNote = noteRepository.findById(ownerNote.getId()).orElseThrow();
      assertThat(updatedNote.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void shouldCreateNewSnapshotWhenContentUpdated() {
      setCurrentUser(owner);
      var request = new CreateNoteRequest(ownerNote.getTitle(), "Updated content");

      var result = underTest.updateNote(ownerNote.getId(), request);

      assertThat(result.title()).isEqualTo(ownerNote.getTitle());
      assertThat(result.content()).isEqualTo("Updated content");

      var snapshots = noteSnapshotRepository.findFirstByNoteIdOrderByCreatedAtDesc(ownerNote);
      assertThat(snapshots.get().getContent()).isEqualTo("Updated content");
    }

    @Test
    void shouldThrowNoteNotFoundExceptionWhenUpdatingNonExistentNote() {
      setCurrentUser(owner);
      var request = new CreateNoteRequest("Title", "Content");

      assertThatThrownBy(() -> underTest.updateNote("non-existent", request))
          .isInstanceOf(NoteNotFoundException.class);
    }

    @Test
    void shouldThrowUnauthorizedNoteAccessExceptionWhenUpdatingOthersNote() {
      setCurrentUser(otherUser);
      var request = new CreateNoteRequest("Title", "Content");

      assertThatThrownBy(() -> underTest.updateNote(ownerNote.getId(), request))
          .isInstanceOf(UnauthorizedNoteAccessException.class);
    }
  }

  @Nested
  class DeleteNote {

    @Test
    void shouldMarkNoteAsInactiveWhenDeleting() {
      setCurrentUser(owner);
      underTest.deleteNote(new DeleteNoteRequest(ownerNote.getId()));

      var deletedNote = noteRepository.findById(ownerNote.getId()).orElseThrow();
      assertThat(deletedNote.isActive()).isFalse();
    }

    @Test
    void shouldThrowNoteNotFoundExceptionWhenDeletingNonExistentNote() {
      setCurrentUser(owner);
      assertThatThrownBy(() -> underTest.deleteNote(new DeleteNoteRequest("non-existent")))
          .isInstanceOf(NoteNotFoundException.class);
    }

    @Test
    void shouldThrowUnauthorizedNoteAccessExceptionWhenDeletingOthersNote() {
      setCurrentUser(owner);
      assertThatThrownBy(() -> underTest.deleteNote(new DeleteNoteRequest(otherUserNote.getId())))
          .isInstanceOf(UnauthorizedNoteAccessException.class);
    }
  }

  @Nested
  class GetNote {

    @Test
    void shouldThrowWhenGettingNoteWithNullId() {
      setCurrentUser(owner);
      assertThatThrownBy(() -> underTest.getNote(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldReturnNoteWithLatestSnapshotWhenNoteExists() {
      setCurrentUser(owner);
      var result = underTest.getNote(ownerNote.getId());

      assertThat(result.id()).isEqualTo(ownerNote.getId());
      assertThat(result.title()).isEqualTo(ownerNote.getTitle());
      assertThat(result.content()).isEqualTo(ownerNoteSnapshot.getContent());
    }

    @Test
    void shouldThrowNoteNotFoundExceptionWhenNoteDoesNotExist() {
      setCurrentUser(owner);
      assertThatThrownBy(() -> underTest.getNote("non-existent"))
          .isInstanceOf(NoteNotFoundException.class);
    }

    @Test
    void shouldThrowUnauthorizedNoteAccessExceptionWhenGettingOthersNote() {
      setCurrentUser(owner);
      assertThatThrownBy(() -> underTest.getNote(otherUserNote.getId()))
          .isInstanceOf(UnauthorizedNoteAccessException.class);
    }
  }

  @Nested
  class GetAllNotes {

    @Test
    void shouldReturnOnlyNotesOwnedByCurrentUserWhenNoFiltersApplied() {
      setCurrentUser(owner);
      var results = underTest.getAllNotes(null, null, null);

      assertThat(results).hasSize(1);
      assertThat(results.get(0).id()).isEqualTo(ownerNote.getId());
    }

    @Test
    void shouldFilterNotesByTitleWhenTitleFilterProvided() {
      setCurrentUser(owner);
      var results = underTest.getAllNotes("First", null, null);

      assertThat(results).hasSize(1);
      assertThat(results.get(0).id()).isEqualTo(ownerNote.getId());
    }

    @Test
    void shouldFilterNotesByContentWhenContentFilterProvided() {
      setCurrentUser(owner);
      var results = underTest.getAllNotes(null, "First", null);

      assertThat(results).hasSize(1);
      assertThat(results.get(0).id()).isEqualTo(ownerNote.getId());
    }

    @Test
    void shouldFilterNotesByImportantFlagWhenImportantFilterTrue() {
      setCurrentUser(otherUser);
      var results = underTest.getAllNotes(null, null, true);

      assertThat(results).hasSize(1);
      assertThat(results.get(0).id()).isEqualTo(otherUserNote.getId());
    }
  }

  @Nested
  class MarkAsImportant {

    @Test
    void shouldThrowWhenMarkingNoteAsImportantWithNullId() {
      setCurrentUser(owner);
      assertThatThrownBy(() -> underTest.markAsImportant(null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldSetImportantFlagWhenMarkingNoteAsImportant() {
      setCurrentUser(owner);
      underTest.markAsImportant(ownerNote.getId());

      var updatedNote = noteRepository.findById(ownerNote.getId()).orElseThrow();
      assertThat(updatedNote.isImportant()).isTrue();
    }

    @Test
    void shouldThrowUnauthorizedNoteAccessExceptionWhenMarkingOthersNoteAsImportant() {
      setCurrentUser(otherUser);
      assertThatThrownBy(() -> underTest.markAsImportant(ownerNote.getId()))
          .isInstanceOf(UnauthorizedNoteAccessException.class);
    }

    @Test
    void shouldThrowNoteNotFoundExceptionWhenMarkingNonExistentNoteAsImportant() {
      setCurrentUser(owner);
      assertThatThrownBy(() -> underTest.markAsImportant("non-existent"))
          .isInstanceOf(NoteNotFoundException.class);
    }
  }
}
