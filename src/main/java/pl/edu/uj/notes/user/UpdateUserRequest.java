package pl.edu.uj.notes.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserRequest {
  private String username;
  private String password;
}
