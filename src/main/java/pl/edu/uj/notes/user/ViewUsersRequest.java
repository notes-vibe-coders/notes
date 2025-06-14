package pl.edu.uj.notes.user;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ViewUsersRequest {
  @NotEmpty private List<String> idList;
}
