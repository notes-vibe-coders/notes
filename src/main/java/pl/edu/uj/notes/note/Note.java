package pl.edu.uj.notes.note;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Data
@Entity
@NoArgsConstructor
public class Note {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String title;

  @CreatedDate private Instant createdAt;

  @LastModifiedDate private Instant updatedAt;

  private boolean active = true;

  public Note(String title) {
    this.title = title;
  }
}
