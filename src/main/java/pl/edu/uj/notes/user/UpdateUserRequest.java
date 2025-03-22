package pl.edu.uj.notes.user;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String username;
    private String password;
}
