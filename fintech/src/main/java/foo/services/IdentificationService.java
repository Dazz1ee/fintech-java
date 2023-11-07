package foo.services;

import foo.dao.UserDao;
import foo.models.CustomUser;
import foo.models.Role;
import foo.models.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.testcontainers.shaded.org.bouncycastle.util.Arrays;

import java.nio.CharBuffer;

@Service
@RequiredArgsConstructor
public class IdentificationService {

    private final UserDao userDao;

    private final PasswordEncoder passwordEncoder;

    public void signUp(UserDto userDto) {
        CustomUser customUser = CustomUser.builder()
                .role(new Role("ROLE_USER"))
                .username(userDto.username())
                .password(passwordEncoder.encode(CharBuffer.wrap(userDto.password())))
                .build();

        Arrays.fill(userDto.password(), '\0');
        userDao.save(customUser);
    }
}
