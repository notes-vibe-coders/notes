package pl.edu.uj.notes.user;

import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteUserRequest {
  @Positive private String id;
}
