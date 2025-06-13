package pl.edu.uj.notes.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CreateUserRequest {
  @NotBlank private String username;
  @NotBlank private String password;
}
