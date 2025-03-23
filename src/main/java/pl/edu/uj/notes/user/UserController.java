package pl.edu.uj.notes.user;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.uj.notes.user.api.CreateUserRequest;
import pl.edu.uj.notes.user.api.ViewUsersRequest;

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

  @GetMapping()
  ResponseEntity<List<String>> viewUsers(@Valid @RequestBody ViewUsersRequest request) {
    List<String> usernames = userService.viewUsers(request);
    return ResponseEntity.ok(usernames);
  }
}
