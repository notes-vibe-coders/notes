package pl.edu.uj.notes.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ViewUsersRequest {
    @NotEmpty private List<String> idList;
}
