package pl.edu.uj.notes.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
/*
TODO:
Probably this and other DTOs don't need to be public (unless it is not required by some mapper)
 */
public class CreateUserRequest {
  @NotBlank private String username;
  @NotBlank private String password;
}
