package pl.edu.uj.notes.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    @NotBlank
    private String userId;

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;
}