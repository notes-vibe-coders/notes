package pl.edu.uj.notes.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import pl.edu.uj.notes.note.Note;
import pl.edu.uj.notes.user.UserEntity;

class NoteAccessAuthorizationStrategyTest {

  final NoteAccessAuthorizationStrategy underTest = new NoteAccessAuthorizationStrategy();
  static final String OWNER_ID = "owner-123";
  static final String OTHER_USER_ID = "other-user-456";
  static final UserEntity USER = new UserEntity();
  static final Note NOTE = new Note("Test Note", null);

  @Nested
  class HasAccessTo {

    @Test
    void hasAccessTo_WhenUserIsAdminButNotOwnerAndActionIsRead_ThenReturnTrue() {
      var admin = USER.withId(OWNER_ID).withAdmin(true);
      var noteOwner = USER.withId(OTHER_USER_ID);
      var note = NOTE.withOwner(noteOwner);

      boolean result = underTest.hasAccessTo(admin, note, Action.READ);

      assertThat(result).isTrue();
    }

    @Test
    void hasAccessTo_WhenUserIsAdminButNotOwnerAndActionIsWrite_ThenReturnFalse() {
      var admin = USER.withId(OWNER_ID).withAdmin(true);
      var noteOwner = USER.withId(OTHER_USER_ID);
      var note = NOTE.withOwner(noteOwner);

      boolean result = underTest.hasAccessTo(admin, note, Action.WRITE);

      assertThat(result).isFalse();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void hasAccessTo_WhenUserIsOwner_ThenReturnTrueForAnyAction(Action action) {
      var owner = USER.withId(OWNER_ID);
      var note = NOTE.withOwner(owner);

      boolean result = underTest.hasAccessTo(owner, note, action);

      assertThat(result).isTrue();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void hasAccessTo_WhenUserIsNotOwner_ThenReturnFalse(Action action) {
      var owner = USER.withId(OWNER_ID);
      var otherUser = USER.withId(OTHER_USER_ID);
      var note = NOTE.withOwner(owner);

      boolean result = underTest.hasAccessTo(otherUser, note, action);

      assertThat(result).isFalse();
    }

    @Test
    void hasAccessTo_WhenUserIsNull_ThenThrowNullPointerException() {
      var note = NOTE.withOwner(USER.withId(OWNER_ID));

      assertThatThrownBy(() -> underTest.hasAccessTo(null, note, Action.READ))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void hasAccessTo_WhenNoteIsNull_ThenThrowNullPointerException() {
      var user = USER.withId(OWNER_ID);

      assertThatThrownBy(() -> underTest.hasAccessTo(user, null, Action.READ))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void hasAccessTo_WhenActionIsNull_ThenThrowNullPointerException() {
      var user = USER.withId(OWNER_ID);
      var note = NOTE.withOwner(user);

      assertThatThrownBy(() -> underTest.hasAccessTo(user, note, null))
          .isInstanceOf(NullPointerException.class);
    }
  }
}
