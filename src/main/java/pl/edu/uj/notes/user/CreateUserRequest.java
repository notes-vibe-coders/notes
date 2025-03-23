package pl.edu.uj.notes.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserRequest {
  @NotBlank private String username;
  @NotBlank private String password;
}
