package pl.edu.uj.notes.authorization;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import pl.edu.uj.notes.note.Note;
import pl.edu.uj.notes.user.UserEntity;

@Component
public class NoteAccessAuthorizationStrategy implements AuthorizationStrategy<Note> {

  @Override
  public boolean hasAccessTo(
      @NonNull UserEntity subject, @NonNull Note resource, @NonNull Action action) {
    return subject.getId().equals(resource.getOwner().getId());
  }
}
