package pl.edu.uj.notes.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HealthController {

  @GetMapping("/health")
  HealthResponse healthcheck() {
    return HealthResponse.OK();
  }
}

record HealthResponse(String status) {

  static HealthResponse OK() {
    return new HealthResponse("OK");
  }
}
