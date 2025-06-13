package pl.edu.uj.notes.note;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.bcrypt.BCrypt;

@Data
@With
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NoteSnapshot {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @ManyToOne
  @JoinColumn(name = "noteId")
  private Note noteId;

  private String content;

  @CreatedDate private Instant createdAt;

  @LastModifiedDate private Instant updatedAt;

  private String passwordHash;

  public void setPassword(String rawPassword) {
    if (rawPassword != null && !rawPassword.isBlank()) {
      this.passwordHash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }
  }

  public boolean isPasswordCorrect(String rawPassword) {
    return passwordHash == null || BCrypt.checkpw(rawPassword, this.passwordHash);
  }

  public NoteSnapshot(Note noteId, String content) {
    this.noteId = noteId;
    this.content = content;
  }
}
