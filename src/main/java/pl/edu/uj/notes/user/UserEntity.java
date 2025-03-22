package pl.edu.uj.notes.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class UserEntity {
  @Id
  @GeneratedValue(generator = "increment")
  private int id;

  @Column(unique = true)
  private String username;

  private String password;

  public UserEntity(String username, String password) {
    this.username = username;
    this.password = password;
  }
}
