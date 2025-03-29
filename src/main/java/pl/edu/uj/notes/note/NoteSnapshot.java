package pl.edu.uj.notes.note;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class NoteSnapshot {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @ManyToOne
  @JoinColumn(name = "noteId")
  private Note noteId;

  private String content;

  public NoteSnapshot(Note noteId, String content) {
    this.noteId = noteId;
    this.content = content;
  }
}
