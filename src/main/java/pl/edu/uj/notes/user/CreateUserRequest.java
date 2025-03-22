package pl.edu.uj.notes.user;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
}
