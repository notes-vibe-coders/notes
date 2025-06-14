package pl.edu.uj.notes.user;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
class UserController {
  private final UserService userService;

  @PostMapping()
  ResponseEntity<String> createUser(@Valid @RequestBody CreateUserRequest request) {
    var id = userService.createUser(request);
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

  @PutMapping("/block")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> blockUser(@Valid @RequestBody BlockUserRequest request) {
    userService.setUserBlockedStatus(request);
    return ResponseEntity.noContent().build();
  }

  @GetMapping()
  ResponseEntity<List<String>> viewUsers(@Valid @RequestBody ViewUsersRequest request) {
    List<String> usernames = userService.viewUsers(request);
    return ResponseEntity.ok(usernames);
  }
}
