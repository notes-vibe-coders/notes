package pl.edu.uj.notes.user;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @PostMapping()
  public ResponseEntity<String> createUser(@RequestBody CreateUserRequest request) {
    int id = userService.create(request);
    URI location = URI.create("/api/v1/user/" + id);
    return ResponseEntity.created(location).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> updateUser(@PathVariable int id, @RequestBody UpdateUserRequest request) {
    userService.updateUser(id, request);
    return ResponseEntity.noContent().build();
  }
}