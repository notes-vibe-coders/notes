package pl.edu.uj.notes.note;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@NoArgsConstructor
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

  public NoteSnapshot(Note noteId, String content) {
    this.noteId = noteId;
    this.content = content;
  }
}
