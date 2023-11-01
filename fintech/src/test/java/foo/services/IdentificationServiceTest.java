package foo.services;

import foo.dao.UserDao;
import foo.exceptions.CreateUserException;
import foo.models.CustomUser;
import foo.models.UserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentificationServiceTest {
    @Mock
    UserDao userDao;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    IdentificationService identificationService;

    @Test
    void signUp() {
        UserDto userDto = new UserDto("test", "test", "test");
        when(passwordEncoder.encode("test")).thenReturn("hash");
        ArgumentCaptor<CustomUser> captor = ArgumentCaptor.forClass(CustomUser.class);

        identificationService.signUp(userDto);
        verify(userDao).save(captor.capture());

        CustomUser customUser = captor.getValue();

        assertThat(customUser.getLogin()).isEqualTo("test");
        assertThat(customUser.getUsername()).isEqualTo("test");
        assertThat(customUser.getPassword()).isEqualTo("hash");
        assertThat(customUser.getRole().getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void failedSignUp() {
        UserDto userDto = new UserDto("test", "test", "test");
        when(userDao.save(any())).thenThrow(CreateUserException.class);
        assertThrows(CreateUserException.class, () -> identificationService.signUp(userDto));
    }
}