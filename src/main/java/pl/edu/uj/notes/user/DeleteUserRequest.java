package pl.edu.uj.notes.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteUserRequest {
  @NotBlank private String id;
}
