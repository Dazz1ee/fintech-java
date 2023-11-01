package foo.services;

import foo.dao.UserDao;
import foo.models.CustomUser;
import foo.models.Role;
import foo.models.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdentificationService {

    private final UserDao userDao;

    private final PasswordEncoder passwordEncoder;

    public void signUp(UserDto userDto) {
        CustomUser customUser = CustomUser.builder().login(userDto.login())
                .role(new Role("ROLE_USER"))
                .username(userDto.username())
                .password(passwordEncoder.encode(userDto.password()))
                .build();
        userDao.save(customUser);
    }
}
