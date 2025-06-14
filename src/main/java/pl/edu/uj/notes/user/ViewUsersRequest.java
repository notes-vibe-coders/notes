package pl.edu.uj.notes.user;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ViewUsersRequest {
  private List<String> idList;
}
