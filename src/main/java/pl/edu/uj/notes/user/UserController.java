package pl.edu.uj.notes.user;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
class UserController {
  private final UserService userService;

  @PostMapping()
  ResponseEntity<String> createUser(@Valid @RequestBody CreateUserRequest request) {
    var id = userService.createUser(request);
    /*
    TODO
    I wonder what is the purpose of returning the URL - canonical REST requires id.
     */
    URI location = URI.create("/api/v1/user/" + id);
    return ResponseEntity.created(location).build();
  }

  @DeleteMapping("/{id}")
  ResponseEntity<Void> deleteUser(@Valid @RequestBody DeleteUserRequest request) {
    userService.deleteUser(request);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/password")
  ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
    userService.updatePassword(request);
    return ResponseEntity.noContent().build();
  }
}
