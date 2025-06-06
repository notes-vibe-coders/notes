package pl.edu.uj.notes.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Entity
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(unique = true)
  private String username;

  private String password;

  private boolean isAdmin;

  @Column(nullable = false)
  private boolean isBlocked = false;


  public UserEntity(String username, String password) {
    this.username = username;
    this.password = password;
    this.isAdmin = false;
  }
}
