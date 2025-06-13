package pl.edu.uj.notes.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
    void shouldReturnTrueWhenUserIsOwner() {
      var owner = USER.withId(OWNER_ID);
      var note = NOTE.withOwner(owner);

      boolean result = underTest.hasAccessTo(owner, note, Action.READ);

      assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserIsNotOwner() {
      var owner = USER.withId(OWNER_ID);
      var otherUser = USER.withId(OTHER_USER_ID);
      var note = NOTE.withOwner(owner);

      boolean result = underTest.hasAccessTo(otherUser, note, Action.READ);
      assertThat(result).isFalse();
    }

    @Test
    void shouldThrowWhenUserIsNull() {
      var note = NOTE.withOwner(USER.withId(OWNER_ID));

      assertThatThrownBy(() -> underTest.hasAccessTo(null, note, Action.READ))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenNoteIsNull() {
      var user = USER.withId(OWNER_ID);

      assertThatThrownBy(() -> underTest.hasAccessTo(user, null, Action.READ))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenActionIsNull() {
      var user = USER.withId(OWNER_ID);
      var note = NOTE.withOwner(user);

      assertThatThrownBy(() -> underTest.hasAccessTo(user, note, null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldWorkWithAnyActionType() {
      var owner = USER.withId(OWNER_ID);
      var note = NOTE.withOwner(owner);

      boolean result = underTest.hasAccessTo(owner, note, Action.WRITE);
      assertThat(result).isTrue();
    }
  }
}
