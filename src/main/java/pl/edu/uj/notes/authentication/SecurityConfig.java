package pl.edu.uj.notes.authentication;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import pl.edu.uj.notes.user.CreateUserRequest;
import pl.edu.uj.notes.user.UserService;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {
  private static final String HEALTHCHECK_ROUTE = "/health";
  private static final String REGISTER_ROUTE = "/api/v1/user";

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
        authz ->
            authz
                .requestMatchers(HttpMethod.GET, HEALTHCHECK_ROUTE)
                .permitAll()
                .requestMatchers(HttpMethod.POST, REGISTER_ROUTE)
                .permitAll()
                .anyRequest()
                .authenticated());

    http.httpBasic(Customizer.withDefaults());

    http.cors(AbstractHttpConfigurer::disable);
    http.csrf(AbstractHttpConfigurer::disable);

    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  ApplicationListener<ApplicationReadyEvent> createAdmin(UserService userService) {
    return event -> {
      var admin = userService.getUserByUsername("admin");
      if (admin.isEmpty()) {
        var user = CreateUserRequest.builder().username("admin").password("admin").build();
        userService.createUser(user);
      }
    };
  }
}
