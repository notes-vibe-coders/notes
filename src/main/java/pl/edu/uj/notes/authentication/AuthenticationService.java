package pl.edu.uj.notes.authentication;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import pl.edu.uj.notes.user.InternalUserService;
import pl.edu.uj.notes.user.UserEntity;

@Component
@RequiredArgsConstructor
class AuthenticationService implements UserDetailsService, PrincipalService {

  private final InternalUserService userService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var user = userService.getUserByUsername(username);
    if (user.isEmpty()) {
      throw new UsernameNotFoundException("User not found");
    }

    return new UserDetailsAdapter(user.get());
  }

  @Override
  public UserEntity fetchCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      var msg = "User not authenticated";
      throw new NotAuthenticatedException(msg);
    }

    Optional<UserEntity> currentUser = userService.getUserByUsername(authentication.getName());
    if (currentUser.isEmpty()) {
      var msg = "Failed to get user for " + authentication.getName();
      throw new NoUserForAuthenticatedPrincipalException(msg);
    }

    return currentUser.get();
  }
}

record UserDetailsAdapter(UserEntity user) implements UserDetails {

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }
}
