package foo.services;

import foo.dao.UserDao;
import foo.exceptions.UserNotFoundException;
import foo.models.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomUserDetailsService implements UserDetailsService {
    private final UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new CustomPrincipal(
                userDao
                        .findByUsername(username)
                        .orElseThrow(() -> new UserNotFoundException("Incorrect login or password"))
        );
    }
}
