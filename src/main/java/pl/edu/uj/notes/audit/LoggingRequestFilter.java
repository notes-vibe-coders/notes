package pl.edu.uj.notes.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingRequestFilter extends OncePerRequestFilter {
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    long startTime = Instant.now().toEpochMilli();
    String path = request.getMethod() + " " + request.getRequestURI();

    filterChain.doFilter(request, response);

    long duration = Instant.now().toEpochMilli() - startTime;
    int statusCode = response.getStatus();
    var requestLog = new RequestLog(path, duration, statusCode, getUsername());

    Level logLevel = statusCode > 399 ? Level.ERROR : Level.INFO;
    log.atLevel(logLevel).log(objectMapper.writeValueAsString(requestLog));
  }

  private String getUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      return authentication.getName();
    }

    return null;
  }
}

record RequestLog(String path, long duration, int statusCode, String username) {}
