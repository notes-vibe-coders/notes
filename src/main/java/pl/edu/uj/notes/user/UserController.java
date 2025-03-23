package pl.edu.uj.notes.user;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
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
    int id = userService.createUser(request);
    URI location = URI.create("/api/v1/user/" + id);
    return ResponseEntity.created(location).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<Map<String, String>> updateUser(
      @PathVariable int id, @RequestBody UpdateUserRequest request) {
    userService.updateUser(id, request);

    Map<String, String> response = new HashMap<>();
    response.put("message", "User updated successfully");

    return ResponseEntity.ok(response);
  }
}
